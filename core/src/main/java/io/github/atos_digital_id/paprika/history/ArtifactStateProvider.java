package io.github.atos_digital_id.paprika.history;

import static org.eclipse.jgit.lib.Constants.HEAD;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
import io.github.atos_digital_id.paprika.project.ArtifactDefProvider;
import io.github.atos_digital_id.paprika.project.ArtifactTags;
import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdHistoryCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdHistoryCache;
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
  private ArtifactDefProvider artifactDefProvider;

  @Inject
  private ArtifactCheckers artifactCheckers;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ArtifactTags artifactTags;

  private final ArtifactIdHistoryCache<LastModifState> lastModifCache =
      new HashMapArtifactIdHistoryCache<>();

  private final ArtifactIdHistoryCache<LastTagState> lastTagCache =
      new HashMapArtifactIdHistoryCache<>();

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

  private final Set<ObjectId> allTags = new HashSet<>();

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

      // test working dir
      if( checker.isDirty( revWalk, head ) ) {
        logger.log( "Dirty working dir" );
        lastModifState = dirty();
      }

      // get most recent modification of a dependency
      ArtifactDef depLastModif = null;
      LastModifState depLastModifState = null;
      int depLastModifSeniority = Integer.MAX_VALUE;
      if( lastModifState == null ) {
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
      }

      // test dirty dependency
      if( depLastModifSeniority == 0 ) {
        logger.log( "Dirty dependency: {}", depLastModif );
        lastModifState = depLastModifState;
      }

      // to be cached
      List<RevCommit> lastModifCacheTargets = new LinkedList<>();
      List<RevCommit> lastTagCacheTargets = new LinkedList<>();

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

      // get all tags of all defs, if not already done
      if( allTags.isEmpty() )
        for( ArtifactDef d : artifactDefProvider.getAllDefs() ) {
          for( Ref ref : artifactTags.getTags( d ) ) {
            RevObject revObj = revWalk.parseAny( ref.getObjectId() );
            ObjectId id = revWalk.peel( revObj ).getId();
            allTags.add( id );
          }
        }

      // loop on each commits

      int currentSeniority = 0;

      RevCommit current = revWalk.next();

      while( current != null ) {

        currentSeniority += 1;

        logger.stack( "Check commit {}: ", Pretty.id( current ) );
        try {

          // check last modif cache
          if( lastModifState == null ) {
            Optional<LastModifState> cachedLastModif = lastModifCache.peek( def, current );
            if( cachedLastModif.isPresent() ) {
              lastModifState = cachedLastModif.get();
              logger.log( "Get last modif from cache" );
            }
          }

          // check last tag cache
          if( lastTagState == null ) {
            Optional<LastTagState> cachedLastTag = lastTagCache.peek( def, current );
            if( cachedLastTag.isPresent() ) {
              lastTagState = cachedLastTag.get();
              logger.log( "Get last tag from cache" );
            }
          }

          // append to 'to be cached' lists
          if( allTags.contains( current ) ) {
            if( lastModifState == null )
              lastModifCacheTargets.add( current );
            lastTagCacheTargets.add( current );
          }

          // tagged?
          if( lastTagState == null && tagsMap.containsKey( current ) ) {

            lastTagState = tagged( def, current, tagsMap.get( current ) );
            lastTagCache.set( def, lastTagCacheTargets, lastTagState );

            logger.log( "Tagged with {}", lastTagState.getRefName() );

            if( lastModifState == null ) {
              lastModifState = lastModif( currentSeniority, current );
              lastModifCache.set( def, lastModifCacheTargets, lastModifState );
            }

          }

          // dependency modified?
          if( lastModifState == null && currentSeniority == depLastModifSeniority ) {

            lastModifState = depLastModifState;
            lastModifCache.set( def, lastModifCacheTargets, lastModifState );
            logger.log( "Modified dependency: {}", depLastModif );

          }

          // modified?
          if( lastModifState == null && checker.isModifiedAt( revWalk, current ) ) {

            lastModifState = lastModif( currentSeniority, current );
            lastModifCache.set( def, lastModifCacheTargets, lastModifState );
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
      lastTagCache.set( def, lastTagCacheTargets, lastTagState );

      if( lastModifState == null ) {
        // should not happen
        lastModifState = lastModif( currentSeniority, current );
        lastModifCache.set( def, lastModifCacheTargets, lastModifState );
      }

      return new LastModifAndTagState( lastModifState, lastTagState );

    } catch( IOException ex ) {
      throw new IllegalStateException( "IO exception: " + ex.getMessage(), ex );
    } finally {
      logger.restore();
    }

  }

}
