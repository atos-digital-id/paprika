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

@Data
public class VersionValue {

  public static VersionValue wrap( Version version ) {
    return version == null ? null : new VersionValue( version );
  }

  @NonNull
  @Getter( NONE )
  private final Version version;

  @Getter( lazy = true )
  private final int major = version.getMajor();

  @Getter( lazy = true )
  private final int minor = version.getMinor();

  @Getter( lazy = true )
  private final int patch = version.getPatch();

  public static class PrereleasesValue extends AbstractCustomStringList {

    public PrereleasesValue( List<String> prereleases ) {
      super( prereleases, ".", UnaryOperator.identity() );
    }

  }

  @Getter( lazy = true )
  private final PrereleasesValue prereleases =
      new PrereleasesValue( Arrays.asList( version.getPrereleases() ) );

  public static class BuildsValue extends AbstractCustomStringList {

    public BuildsValue( List<String> builds ) {
      super( builds, ".", UnaryOperator.identity() );
    }

  }

  @Getter( lazy = true )
  private final BuildsValue builds = new BuildsValue( Arrays.asList( version.getBuilds() ) );

  @Override
  public String toString() {
    return version.toString();
  }

}
