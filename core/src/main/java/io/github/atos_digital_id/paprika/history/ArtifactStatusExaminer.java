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
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

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

      // examine

      if( !state.isTagged() ) {
        logger.log( "Never tagged" );
        return snapshotStatus( def, state, lastModif );
      }

      if( !state.getLastTag().getId().equals( lastModif.getId() ) ) {
        logger.log( "Modified since last tag" );
        return snapshotStatus( def, state, lastModif );
      }

      for( ArtifactDef dependency : def.getAllDependencies() ) {
        if( examine( dependency ).isSnapshot() ) {
          logger.log( "Modified dependency: {}", dependency );
          return snapshotStatus( def, state, lastModif );
        }
      }

      logger.log( "Pristine" );

      return new ArtifactStatus(
          state.getLastModif().getId(),
          state.getLastModif().getDate(),
          state.getLastTag().getId(),
          state.getLastTag().getRefName(),
          state.getLastTag().getVersion(),
          false,
          state.getLastTag().getVersion() );

    } finally {
      logger.restore();
    }

  }

  private ArtifactStatus snapshotStatus(
      ArtifactDef def,
      LastModifAndTagState state,
      LastModifState lastModif ) {

    Version lastTaggedVersion = state.getLastTag().getVersion();
    boolean isTagged = state.isTagged();
    Config config = configHandler.get( def );

    List<String> prereleases = new ArrayList<>();

    prereleases.add( Version.SNAPSHOT );

    String branch = gitHandler.branch();
    if( !branch.isEmpty() && configHandler.get( def ).isQualifiedBranch( branch ) )
      prereleases.add( protectBranchName( branch ) );

    Version initVersion = config.getInitVersion();
    int major = initVersion.getMajor();
    int minor = initVersion.getMinor();
    int patch = initVersion.getPatch();

    if( isTagged )
      switch( this.incrementPart ) {

        case MAJOR:
          major = major + 1;
          minor = 0;
          patch = 0;
          break;

        case MINOR:
          minor = minor + 1;
          patch = 0;
          break;

        case PATCH:
          patch = patch + 1;
          break;

        default:
          throw new IllegalArgumentException(
              "Unexpected increment part: " + config.getReleaseIncrement() );

      }

    Version snapshotVersion = new Version(
        major,
        minor,
        patch,
        prereleases.toArray( Version.EMPTY_STRINGS ),
        Version.EMPTY_STRINGS );

    return new ArtifactStatus(
        lastModif.getId(),
        lastModif.getDate(),
        state.getLastTag().getId(),
        lastModif.getRefName(),
        state.getLastTag().getVersion(),
        true,
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
