package io.github.atos_digital_id.paprika.history;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactTags;
import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import lombok.Data;
import lombok.NonNull;

/**
 * Computes the modification state of a module.
 **/
@Named
@Singleton
public class ArtifactStateProvider {

  /**
   * Null object id.
   **/
  public static final ObjectId ZERO_ID = ObjectId.zeroId();

  @Inject
  private PaprikaLogger logger;

  @Inject
  private ArtifactCheckers artifactCheckers;

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ArtifactTags artifactTags;

  private static final LastModifState DIRTY_STATE = new LastModifState( 0, null );

  private static final LastTagState NEVER_TAGGED_STATE = new LastTagState( null, null, null );

  private LastTagState tagged( ArtifactDef def, RevCommit tagCommit, Ref tagref )
      throws IOException {

    return new LastTagState(
        tagCommit,
        tagref.getName().substring( 10 ), // remove refs/tags/
        artifactTags.getVersion( def, tagref ) );

  }

  /**
   * Result of a scan for modifications and tags of a module.
   **/
  @Data
  public static class LastModifAndTagState {

    /**
     * Last modification state.
     *
     * @return the last modification state.
     **/
    @NonNull
    private final LastModifState lastModif;

    /**
     * Last tag state.
     *
     * @return the last tag state.
     **/
    @NonNull
    private final LastTagState lastTag;

  }

  private final ArtifactIdCache<LastModifAndTagState> cache = new HashMapArtifactIdCache<>();

  /**
   * Returns the result of the scan of a module.
   *
   * @param def the module to scan.
   * @return the result of the scan.
   **/
  public LastModifAndTagState get( @NonNull ArtifactDef def ) {
    return cache.get( def, () -> internalGet( def ) );
  }

  private LastModifAndTagState internalGet( ArtifactDef def ) {

    logger.reset( "State of {}: ", def );
    try( RevWalk revWalk = new RevWalk( gitHandler.repository() ) ) {

      LastModifState lastModifState = null;
      LastTagState lastTagState = null;

      ArtifactCheckers.Checker checker = artifactCheckers.create( def );

      ObjectId headId = gitHandler.head();
      // no commits yet
      if( headId == null || ZERO_ID.equals( headId ) )
        return new LastModifAndTagState( DIRTY_STATE, NEVER_TAGGED_STATE );

      RevCommit head = revWalk.parseCommit( headId );

      // get most recent modification of a dependency
      ArtifactDef depLastModif = null;
      LastModifState depLastModifState = null;
      int depLastModifSeniority = Integer.MAX_VALUE;
      for( ArtifactDef dependency : def.getAllDependencies() ) {

        LastModifAndTagState depState = get( dependency );
        LastModifState depModifState = depState.getLastModif();
        int depSeniority = depModifState.getSeniority();

        if( depLastModifSeniority > depSeniority ) {
          depLastModif = dependency;
          depLastModifState = depModifState;
          depLastModifSeniority = depSeniority;
        }

        if( depLastModifSeniority == 0 )
          break;

      }

      // test dirty dependency
      if( depLastModifSeniority == 0 ) {
        logger.log( "Dirty dependency: {}", depLastModif );
        lastModifState = depLastModifState;
      }

      // test working dir
      if( lastModifState == null && checker.isDirty( revWalk, head ) ) {
        logger.log( "Dirty working dir" );
        lastModifState = DIRTY_STATE;
      }

      // get tags of def
      Map<RevCommit, Ref> tagsMap = new HashMap<>();
      List<Ref> tagRefs = artifactTags.getTags( def );
      logger.log( "Tags found: {}", Pretty.refs( tagRefs ) );
      for( Ref ref : tagRefs )
        tagsMap.put( revWalk.parseCommit( ref.getObjectId() ), ref );

      revWalk.setFirstParent( true );
      revWalk.markStart( head );

      // loop on each commits

      int currentSeniority = 0;

      RevCommit current = revWalk.next();

      while( current != null ) {

        currentSeniority += 1;

        logger.stack( "Check commit {}: ", Pretty.id( current ) );
        try {

          // tagged?
          if( lastTagState == null && tagsMap.containsKey( current ) ) {

            lastTagState = tagged( def, current, tagsMap.get( current ) );
            logger.log( "Tagged with {}", lastTagState.getRefName() );

            if( lastModifState == null )
              lastModifState = new LastModifState( currentSeniority, current );

          }

          // dependency modified?
          if( lastModifState == null && currentSeniority == depLastModifSeniority ) {
            lastModifState = depLastModifState;
            logger.log( "Modified dependency: {}", depLastModif );
          }

          // modified?
          if( lastModifState == null && checker.isModifiedAt( revWalk, current ) ) {
            lastModifState = new LastModifState( currentSeniority, current );
            logger.log( "Modified" );
          }

          // everything's found?
          if( lastModifState != null && lastTagState != null )
            return new LastModifAndTagState( lastModifState, lastTagState );

          current = revWalk.next();

        } finally {
          logger.unstack();
        }

      }

      // never tagged

      logger.log( "Never tagged" );

      lastTagState = NEVER_TAGGED_STATE;

      if( lastModifState == null ) {
        // should not happen
        lastModifState = new LastModifState( currentSeniority, current );
      }

      return new LastModifAndTagState( lastModifState, lastTagState );

    } catch( IOException ex ) {
      throw new IllegalStateException( "IO exception: " + ex.getMessage(), ex );
    } finally {
      logger.restore();
    }

  }

}
