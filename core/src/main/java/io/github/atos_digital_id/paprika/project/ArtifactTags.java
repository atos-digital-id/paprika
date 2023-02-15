package io.github.atos_digital_id.paprika.project;

import static org.eclipse.jgit.lib.Constants.R_TAGS;

import java.io.IOException;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.eclipse.jgit.lib.Ref;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.NonNull;

/**
 * Tagging conventions and utilities.
 **/
@Named
@Singleton
public class ArtifactTags {

  @Inject
  private GitHandler gitHandler;

  /**
   * Get all Git tags associated with a module id.
   *
   * @param id the module id to seek.
   * @return all the tags of the module.
   * @throws IOException the Git reference space cannot be accessed.
   **/
  public List<Ref> getTags( @NonNull ArtifactId id ) throws IOException {
    String prefix = R_TAGS + id.getArtifactId() + "/";
    return gitHandler.repository().getRefDatabase().getRefsByPrefix( prefix );
  }

  /**
   * Extact a version from a tag name.
   *
   * @param id the id of the module.
   * @param tag the tag name to parse.
   * @return the parsed version.
   **/
  public Version getVersion( @NonNull ArtifactId id, @NonNull String tag ) {

    int offset = 0;

    if( tag.startsWith( R_TAGS ) )
      offset += R_TAGS.length();

    String prefix = id.getArtifactId() + "/";
    if( !tag.startsWith( prefix, offset ) )
      return new Version( 0, 0, 0, new String[] { Version.WRONG_TAG }, new String[] { tag } );
    offset += prefix.length();

    Version version = Version.parse( tag.substring( offset ) );

    return version;

  }

  /**
   * Extact a version from a ref commit.
   *
   * @param id the id of the module.
   * @param ref the ref commit.
   * @return the parsed version.
   **/
  public Version getVersion( @NonNull ArtifactId id, @NonNull Ref ref ) {
    return getVersion( id, ref.getName() );
  }

  /**
   * Compute the complete tag name from a module id and a version. Includes
   * {@link org.eclipse.jgit.lib.Constants#R_TAGS} as prefix.
   *
   * @param id the id of the module.
   * @param version the version of the module.
   * @return the complete tag name.
   **/
  public String getCompleteTag( @NonNull ArtifactId id, @NonNull Version version ) {
    return R_TAGS + id.getArtifactId() + "/" + version;
  }

  /**
   * Compute the complete tag name from a module id and a version. Includes
   * {@link org.eclipse.jgit.lib.Constants#R_TAGS} as prefix.
   *
   * @param id the id of the module.
   * @param version the unparsed version of the module.
   * @return the complete tag name.
   **/
  public String getCompleteTag( @NonNull ArtifactId id, @NonNull String version ) {
    return R_TAGS + id.getArtifactId() + "/" + version;
  }

  /**
   * Compute the tag name from a module id and a version. Don't include
   * {@link org.eclipse.jgit.lib.Constants#R_TAGS}.
   *
   * @param id the id of the module.
   * @param version the version of the module.
   * @return the tag name.
   **/
  public String getShortTag( @NonNull ArtifactId id, @NonNull Version version ) {
    return id.getArtifactId() + "/" + version;
  }

  /**
   * Compute the tag name from a module id and a version. Don't include
   * {@link org.eclipse.jgit.lib.Constants#R_TAGS}.
   *
   * @param id the id of the module.
   * @param version the unparsed version of the module.
   * @return the tag name.
   **/
  public String getShortTag( @NonNull ArtifactId id, @NonNull String version ) {
    return id.getArtifactId() + "/" + version;
  }

}
