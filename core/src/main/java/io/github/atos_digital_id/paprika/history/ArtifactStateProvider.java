package io.github.atos_digital_id.paprika.history;

import static org.eclipse.jgit.lib.Constants.HEAD;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactTags;
import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import io.github.atos_digital_id.paprika.version.Version;
import io.github.atos_digital_id.paprika.version.VersionParsingException;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Computes the modification state of a module.
 **/
@Named
@Singleton
public class ArtifactStateProvider {

  public static final ObjectId ZERO_ID = ObjectId.zeroId();

  @Inject
  private PaprikaLogger logger;

  @Inject
  private ArtifactCheckers artifactCheckers;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ArtifactTags artifactTags;

  private LastModifState dirty() {
    return new LastModifState( 0, ZERO_ID, HEAD, gitHandler.startTime() );
  }

  private LastModifState lastModif( int seniority, ObjectId lastMatchCommit ) {

    if( !ZERO_ID.equals( lastMatchCommit ) && ( lastMatchCommit instanceof RevCommit ) )
      return new LastModifState(
          seniority,
          lastMatchCommit,
          Pretty.id( lastMatchCommit ).toString(),
          extractDate( (RevCommit) lastMatchCommit ) );

    return new LastModifState( seniority, lastMatchCommit, HEAD, gitHandler.startTime() );

  }

  private ZonedDateTime extractDate( RevCommit commit ) {

    PersonIdent committer = commit.getCommitterIdent();
    if( committer == null )
      committer = commit.getAuthorIdent();
    if( committer != null ) {
      TimeZone timeZone = committer.getTimeZone();
      if( timeZone == null )
        timeZone = TimeZone.getDefault();
      Date when = committer.getWhen();
      if( when != null )
        return ZonedDateTime.ofInstant( when.toInstant(), timeZone.toZoneId() );
    }

    return gitHandler.startTime();

  }

  private LastTagState tagged( ArtifactDef def, ObjectId tagCommit, Ref tagref )
      throws IOException {

    // remove refs/tags/
    String refName = tagref.getName().substring( 10 );

    Version baseVersion;
    try {
      baseVersion = artifactTags.getVersion( def, tagref );
    } catch( VersionParsingException ex ) {
      throw new IOException( "The tag '" + tagref + "' doesn't contains a valid version." );
    }

    return new LastTagState( tagCommit, refName, baseVersion );

  }

  private LastTagState neverTagged( ArtifactDef def ) {
    return new LastTagState( ZERO_ID, HEAD, configHandler.get( def ).getInitVersion() );
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
     * Versionned module flag.
     *
     * @return true if the module is versionned.
     **/
    @Getter( lazy = true )
    private final boolean versionned = !ZERO_ID.equals( this.lastModif.getId() );

    /**
     * Last tag state.
     *
     * @return the last tag state.
     **/
    @NonNull
    private final LastTagState lastTag;

    /**
     * Tagged module flag.
     *
     * @return true if the module has been tagged.
     **/
    @Getter( lazy = true )
    private final boolean tagged = !ZERO_ID.equals( this.lastTag.getId() );

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
        return new LastModifAndTagState( dirty(), neverTagged( def ) );

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
        lastModifState = dirty();
      }

      revWalk.setFirstParent( true );
      revWalk.markStart( head );

      // get tags of def
      Map<ObjectId, Ref> tagsMap = new HashMap<>();
      List<Ref> tagRefs = artifactTags.getTags( def );
      logger.log( "Tags found: {}", Pretty.refs( tagRefs ) );
      for( Ref ref : tagRefs ) {
        RevObject revObj = revWalk.parseAny( ref.getObjectId() );
        ObjectId id = revWalk.peel( revObj ).getId();
        tagsMap.put( id, ref );
      }

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
              lastModifState = lastModif( currentSeniority, current );

          }

          // dependency modified?
          if( lastModifState == null && currentSeniority == depLastModifSeniority ) {
            lastModifState = depLastModifState;
            logger.log( "Modified dependency: {}", depLastModif );
          }

          // modified?
          if( lastModifState == null && checker.isModifiedAt( revWalk, current ) ) {
            lastModifState = lastModif( currentSeniority, current );
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

      lastTagState = neverTagged( def );

      if( lastModifState == null ) {
        // should not happen
        lastModifState = lastModif( currentSeniority, current );
      }

      return new LastModifAndTagState( lastModifState, lastTagState );

    } catch( IOException ex ) {
      throw new IllegalStateException( "IO exception: " + ex.getMessage(), ex );
    } finally {
      logger.restore();
    }

  }

}
