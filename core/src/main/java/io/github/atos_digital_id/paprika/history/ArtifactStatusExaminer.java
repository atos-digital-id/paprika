package io.github.atos_digital_id.paprika.history;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.history.ArtifactStateProvider.LastModifAndTagState;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.NonNull;

/**
 * Computes the state and the version of a module.
 **/
@Named
@Singleton
public class ArtifactStatusExaminer {

  /**
   * Part of the version to increment in case of a snapshot.
   **/
  public enum IncrementPart {

    MAJOR,

    MINOR,

    PATCH;

  }

  @Inject
  private PaprikaLogger logger;

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private ArtifactStateProvider artifactStateProvider;

  private final ArtifactIdCache<ArtifactStatus> cache = new HashMapArtifactIdCache<>();

  /**
   * Returns the status of a module.
   *
   * @param def the module to examine.
   * @return the status of the module.
   **/
  public ArtifactStatus examine( @NonNull ArtifactDef def ) {
    return cache.get( def, () -> internalExamine( def ) );
  }

  private ArtifactStatus internalExamine( ArtifactDef def ) {

    logger.reset( "Examine {}: ", def );
    try {

      LastModifAndTagState state = artifactStateProvider.get( def );
      LastModifState lastModif = state.getLastModif();
      LastTagState lastTag = state.getLastTag();

      // examine

      if( lastTag.getCommit() == null ) {
        logger.log( "Never tagged" );
        return snapshotStatus( def, lastModif, lastTag );
      }

      if( !lastTag.getCommit().equals( lastModif.getCommit() ) ) {
        logger.log( "Modified since last tag" );
        return snapshotStatus( def, lastModif, lastTag );
      }

      for( ArtifactDef dependency : def.getAllDependencies() ) {
        if( examine( dependency ).getVersion().isSnapshot() ) {
          logger.log( "Modified dependency: {}", dependency );
          return snapshotStatus( def, lastModif, lastTag );
        }
      }

      logger.log( "Pristine" );

      return new ArtifactStatus(
          lastModif.getCommit(),
          lastTag.getCommit(),
          lastTag.getRefName(),
          lastTag.getVersion(),
          lastTag.getVersion() );

    } finally {
      logger.restore();
    }

  }

  private ArtifactStatus snapshotStatus(
      ArtifactDef def,
      LastModifState lastModif,
      LastTagState lastTag ) {

    Config config = configHandler.get( def );

    Version baseVersion = lastTag.getVersion();

    int major, minor, patch;
    if( baseVersion == null ) {

      Version initVersion = config.getInitVersion();
      major = initVersion.getMajor();
      minor = initVersion.getMinor();
      patch = initVersion.getPatch();

    } else {

      switch( config.getReleaseIncrement() ) {

        case MAJOR:
          major = baseVersion.getMajor() + 1;
          minor = 0;
          patch = 0;
          break;

        case MINOR:
          major = baseVersion.getMajor();
          minor = baseVersion.getMinor() + 1;
          patch = 0;
          break;

        case PATCH:
          major = baseVersion.getMajor();
          minor = baseVersion.getMinor();
          patch = baseVersion.getPatch() + 1;
          break;

        default:
          throw new IllegalArgumentException(
              "Unexpected increment part: " + config.getReleaseIncrement() );

      }

    }

    List<String> prereleases = new ArrayList<>();

    prereleases.add( Version.SNAPSHOT );

    String branch = gitHandler.branch();
    if( !branch.isEmpty() && config.isQualifiedBranch( branch ) )
      prereleases.add( protectBranchName( branch ) );

    Version snapshotVersion = new Version(
        major,
        minor,
        patch,
        prereleases.toArray( Version.EMPTY_STRINGS ),
        Version.EMPTY_STRINGS );

    return new ArtifactStatus(
        lastModif.getCommit(),
        lastTag.getCommit(),
        lastTag.getRefName(),
        lastTag.getVersion(),
        snapshotVersion );

  }

  private String protectBranchName( String branch ) {

    int len = branch.length();

    StringBuilder builder = new StringBuilder( len );

    for( int i = 0; i < len; i++ ) {

      char c = branch.charAt( i );
      if( c == '\\'
          || c == '/'
          || c == ':'
          || c == '\''
          || c == '"'
          || c == '<'
          || c == '>'
          || c == '|'
          || c == '?'
          || c == '*' )
        builder.append( '-' );
      else
        builder.append( c );

    }

    return builder.toString();

  }

}
