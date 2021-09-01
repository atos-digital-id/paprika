package io.github.atos_digital_id.paprika.config;

import io.github.atos_digital_id.paprika.utils.Patterns;
import io.github.atos_digital_id.paprika.utils.Patterns.PathFilter;
import io.github.atos_digital_id.paprika.version.Version;
import io.github.atos_digital_id.paprika.version.VersionParsingException;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * User configuration of a module.
 **/
@Data
@Builder( toBuilder = true )
public class Config {

  /*
   * Core
   */

  /**
   * Configured non qualifier branch names. With wild cards. Default value:
   * {@code "master"}.
   *
   * @param nonQualifierBrancheNames configured qualifier branch names.
   * @return the configured non qualifier branch names.
   **/
  @Builder.Default
  private final String nonQualifierBrancheNames = "main:master";

  /**
   * Qualified branches predicate. Test if a branch is not matched by
   * {@link Config#getNonQualifierBrancheNames}.
   *
   * @return a qualified branch name predicate.
   **/
  @Getter( lazy = true )
  private final PathFilter nonQualifiedBranchPredicate = parseNonQualifiedBranches();

  private PathFilter parseNonQualifiedBranches() {
    return Patterns.pathFilter( getNonQualifierBrancheNames() );
  }

  /**
   * Test if a branch should be qualified. Returns
   * {@code getQualifiedBranchPredicate().test( branch );}.
   *
   * @param branch the tested branch name.
   * @return if the branch should be qualified.
   **/
  public boolean isQualifiedBranch( String branch ) {
    return !getNonQualifiedBranchPredicate().complete( branch );
  }

  /**
   * Configured initial version. Default value: {@code "0.1.0"}.
   *
   * @param initVersionValue configured initial version.
   * @return the configured initial version.
   **/
  @NonNull
  @Builder.Default
  private final String initVersionValue = "0.1.0";

  /**
   * Parsed initial version.
   *
   * @return initVersion parsed initial version.
   **/
  @Getter( lazy = true )
  private final Version initVersion = parseInitVersionValue();

  private Version parseInitVersionValue() {

    try {
      return Version.parse( getInitVersionValue() );
    } catch( VersionParsingException ex ) {
      throw new IllegalArgumentException(
          "Can not parse configured first version: " + ex.getMessage(),
          ex );
    }

  }

  /**
   * Configured observed path. With wild cards. Default value:
   * {@code "pom.xml:.mvn/**:src/main/**"}.
   *
   * @param observedPathValue configured observed path.
   * @return the configured observed path.
   **/
  @Builder.Default
  private final String observedPathValue = "pom.xml:.mvn/**:src/main/**";

  /**
   * Observed path predicate. Test if a directory is matched by
   * {@link Config#getObservedPathValue}.
   *
   * @return an observed path predicate.
   **/
  @Getter( lazy = true )
  private final PathFilter observedPath = parseObservedPath();

  private PathFilter parseObservedPath() {
    return Patterns.pathFilter( getObservedPathValue() );
  }

  /**
   * Configured value of reproducible builds flag. Default value:
   * {@code "true"}.
   *
   * @param reproducibleValue configured value of reproducible builds flag.
   * @return the configured value of reproducible builds flag.
   **/
  @NonNull
  @Builder.Default
  private final String reproducibleValue = "true";

  /**
   * Parsed value of reproducible builds flag. Test if
   * {@link Config#getReproducibleValue} is different of {@code "false"} (case
   * insensitive).
   *
   * @return the parsed value of reproducible builds flag.
   **/
  @Getter( lazy = true )
  private final boolean reproducible = !"false".equalsIgnoreCase( getReproducibleValue() );

  /*
   * Release
   */

  /**
   * Configured value of last modification flag. Default value: {@code "true"}.
   *
   * @param lastModificationValue configured value of last modification flag.
   * @return the configured value of last modification flag.
   */
  @NonNull
  @Builder.Default
  private final String lastModificationValue = "true";

  /**
   * Parsed value of last modification flag. Test if
   * {@link Config#getLastModificationValue} is different of {@code "false"}
   * (case insensitive).
   *
   * @return the parsed value of last modification flag.
   **/
  @Getter( lazy = true )
  private final boolean lastModification = !"false".equalsIgnoreCase( getLastModificationValue() );

  /**
   * Configured value of annotated flag. Default value: {@code "true"}.
   *
   * @param annotatedValue configured value of annotated flag.
   * @return the configured value of annotated flag.
   */
  @NonNull
  @Builder.Default
  private final String annotatedValue = "true";

  /**
   * Parsed value of annotated flag. Test if {@link Config#getAnnotatedValue} is
   * different of {@code "false"} (case insensitive).
   *
   * @param annotated parsed value of annotated flag.
   * @return the configured value of annotated flag.
   **/
  @Getter( lazy = true )
  private final boolean annotated = !"false".equalsIgnoreCase( getAnnotatedValue() );

  /**
   * Configured value of signed flag. Default value: {@code "true"}.
   *
   * @param signedValue configured value of signed flag.
   * @return the configured value of signed flag.
   **/
  @NonNull
  @Builder.Default
  private final String signedValue = "false";

  /**
   * Parsed value of signed flag. Test if {@link Config#getSignedValue} is
   * different of {@code "false"} (case insensitive).
   *
   * @param signed parsed value of signed flag.
   * @return the parsed value of signed flag.
   **/
  @Getter( lazy = true )
  private final boolean signed = !"false".equalsIgnoreCase( getSignedValue() );

  /**
   * Configured release message. Default value: {@code "Release ${artifactId}
   * ${version}"}
   *
   * @param releaseMessage configured release message.
   * @return the configured release message.
   **/
  @NonNull
  @Builder.Default
  private final String releaseMessage = "Release ${artifactId} ${version}";

  /**
   * Configured value of ignored flag. Default value: {@code "false"}.
   *
   * @param releaseIgnoredValue configured value of ignored flag.
   * @return the configured value of ignored flag.
   **/
  @NonNull
  @Builder.Default
  private final String releaseIgnoredValue = "false";

  /**
   * Parsed value of ignored flag. Test if {@link Config#getReleaseIgnoredValue}
   * is different of {@code "false"} (case insensitive).
   *
   * @param releaseIgnored parsed value of ignored flag.
   * @return the parsed value of ignored flag.
   **/
  @Getter( lazy = true )
  private final boolean releaseIgnored = !"false".equalsIgnoreCase( getReleaseIgnoredValue() );

}
