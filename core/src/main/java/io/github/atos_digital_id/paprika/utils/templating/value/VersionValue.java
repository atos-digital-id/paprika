package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.AbstractCustomStringList;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrapper around a version.
 */
@Data
public class VersionValue {

  /**
   * Return a wrapper around a version.
   *
   * @param version the version to wrap.
   * @return the wrapped version, or {@code null} if the version is
   *         {@code null}.
   */
  public static VersionValue wrap( Version version ) {
    return version == null ? null : new VersionValue( version );
  }

  @NonNull
  @Getter( NONE )
  private final Version version;

  /**
   * Major part.
   *
   * @return the major part.
   */
  @Getter( lazy = true )
  private final int major = version.getMajor();

  /**
   * Minor part.
   *
   * @return the minor part.
   */
  @Getter( lazy = true )
  private final int minor = version.getMinor();

  /**
   * Patch part.
   *
   * @return the patch part.
   */
  @Getter( lazy = true )
  private final int patch = version.getPatch();

  /**
   * Prerelease values in a requestable and iterable form.
   */
  public static class PrereleasesValue extends AbstractCustomStringList {

    public PrereleasesValue( List<String> prereleases ) {
      super( prereleases, ".", UnaryOperator.identity() );
    }

  }

  /**
   * Prerelease values.
   *
   * @return the prerelease values.
   */
  @Getter( lazy = true )
  private final PrereleasesValue prereleases =
      new PrereleasesValue( Arrays.asList( version.getPrereleases() ) );

  /**
   * Build values in a requestable and iterable form.
   */
  public static class BuildsValue extends AbstractCustomStringList {

    public BuildsValue( List<String> builds ) {
      super( builds, ".", UnaryOperator.identity() );
    }

  }

  /**
   * Build values.
   *
   * @return the build values.
   */
  @Getter( lazy = true )
  private final BuildsValue builds = new BuildsValue( Arrays.asList( version.getBuilds() ) );

  /**
   * Return the complete version.
   *
   * @return the complete version.
   */
  @Override
  public String toString() {
    return version.toString();
  }

}
