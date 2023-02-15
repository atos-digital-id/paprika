package io.github.atos_digital_id.paprika.config;

import static lombok.AccessLevel.PRIVATE;

import java.nio.file.Path;

import io.github.atos_digital_id.paprika.history.ArtifactStatusExaminer.IncrementPart;
import io.github.atos_digital_id.paprika.utils.Patterns;
import io.github.atos_digital_id.paprika.utils.Patterns.PathFilter;
import io.github.atos_digital_id.paprika.utils.templating.engine.Escaper;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * User configuration of a directory.
 **/
@Data
public class Config {

  @NonNull
  @Getter( PRIVATE )
  private final ConfigProperties configProperties;

  /**
   * Target path of the configuration.
   *
   * @return the path which is configured.
   **/
  @NonNull
  private final Path dir;

  private String getConfigValue( String sys, String env, String prop, String def ) {

    String value = null;
    if( value == null && sys != null )
      value = System.getProperty( sys );
    if( value == null && env != null )
      value = System.getenv( env );
    if( value == null && prop != null )
      value = configProperties.get( dir ).get( prop );
    if( value == null )
      value = def;

    return value;

  }

  private boolean getBoolValue( String sys, String env, String prop, boolean def ) {
    return !"false".equalsIgnoreCase( getConfigValue( sys, env, prop, def ? "true" : "false" ) );
  }

  /*
   * Core
   */

  private static Boolean skip;

  /**
   * Skip all Paprika substitution. Default value: {@code false}. Environment
   * variable : {@code PAPRIKA_SKIP}. System property: {@code paprika.skip}.
   *
   * @return {@code true} if the substitutions should be skipped.
   */
  public static boolean isSkipped() {

    if( skip != null )
      return skip;

    String value = System.getProperty( "paprika.skip" );
    if( value == null )
      value = System.getenv( "PAPRIKA_SKIP" );

    skip = value != null && !"false".equalsIgnoreCase( value );
    return skip;

  }

  /**
   * Non qualifier branch names. Supports wild cards. Default value:
   * {@code "main:master"}. Property name: {@code nonQualifierBranches}.
   * Environment variable: {@code PAPRIKA_NON_QUALIFIER_BRANCHES}. System
   * property: {@code paprika.nonQualifierBranches}.
   *
   * @return the non qualifier branch names.
   **/
  @Getter( lazy = true )
  private final String nonQualifierBranches = computeNonQualifierBranches();

  private String computeNonQualifierBranches() {
    return getConfigValue(
        "paprika.nonQualifierBranches",
        "PAPRIKA_NON_QUALIFIER_BRANCHES",
        "nonQualifierBranches",
        "main:master" );
  }

  /**
   * Qualified branches predicate. Test if a branch is not matched by
   * {@link Config#getNonQualifierBranches}.
   *
   * @return a qualified branch name predicate.
   **/
  @Getter( lazy = true )
  private final PathFilter nonQualifiedBranchPredicate = parseNonQualifiedBranches();

  private PathFilter parseNonQualifiedBranches() {
    return Patterns.pathFilter( getNonQualifierBranches() );
  }

  /**
   * Test if a branch should be qualified. Returns
   * {@code getQualifiedBranchPredicate().test( branch );}.
   *
   * @param branch the branch name to test.
   * @return if the branch should be qualified.
   **/
  public boolean isQualifiedBranch( String branch ) {
    return !getNonQualifiedBranchPredicate().complete( branch );
  }

  /**
   * Initial version. Default value: {@code "0.1.0"}. Property name:
   * {@code initVersion}. Environment variable {@code PAPRIKA_INIT_VERSION}.
   * System property: {@code paprika.initVersion}.
   *
   * @return the initial version.
   **/
  @Getter( lazy = true )
  private final Version initVersion = computeInitVersion();

  private Version computeInitVersion() {
    return Version.parse(
        getConfigValue( "paprika.initVersion", "PAPRIKA_INIT_VERSION", "initVersion", "0.1.0" ) );
  }

  /**
   * Observed path. Supports wild cards. Default value:
   * {@code "pom.xml:.mvn/**:src/main/**"}. Property name: {@code observedPath}.
   * Environment variable: {@code PAPRIKA_OBSERVED_PATH}. System property:
   * {@code paprika.observedPath}.
   *
   * @return the observed path.
   **/
  @Getter( lazy = true )
  private final String observedPath = computeObservedPath();

  private String computeObservedPath() {
    return getConfigValue(
        "paprika.observedPath",
        "PAPRIKA_OBSERVED_PATH",
        "observedPath",
        "pom.xml:.mvn/**:src/main/**" );
  }

  /**
   * Observed path predicate. Test if a directory is matched by
   * {@link Config#getObservedPath}.
   *
   * @return an observed path predicate.
   **/
  @Getter( lazy = true )
  private final PathFilter observedPathPredicate = parseObservedPath();

  private PathFilter parseObservedPath() {
    return Patterns.pathFilter( getObservedPath() );
  }

  /**
   * Reproducible builds flag. Default value: {@code true}. Property name:
   * {@code reproducible}. Environment variable: {@code PAPRIKA_REPRODUCIBLE}.
   * System property: {@code paprika.reproducible}.
   *
   * @return the reproducible builds flag.
   **/
  @Getter( lazy = true )
  private final boolean reproducible = computeReproducible();

  private boolean computeReproducible() {
    return getBoolValue(
        "paprika.reproducible",
        "PAPRIKA_REPRODUCIBLE",
        "paprika.reproducible",
        true );
  }

  /*
   * Release
   */

  /**
   * Tag last modification commit, instead of HEAD. Default value: {@code true}.
   * Property name: {@code release.lastModification}. Environment variable:
   * {@code PAPRIKA_RELEASE_LAST_MODIFICATION}. System property:
   * {@code lastModification}.
   *
   * @return the last modification flag.
   */
  @Getter( lazy = true )
  private final boolean releaseLastModification = computeReleaseLastModification();

  private boolean computeReleaseLastModification() {
    return getBoolValue(
        "lastModification",
        "PAPRIKA_RELEASE_LAST_MODIFICATION",
        "release.lastModification",
        true );
  }

  /**
   * Use annotated tag. Default value: {@code true}. Property name:
   * {@code release.annotated}. Environment variable:
   * {@code PAPRIKA_RELEASE_ANNOTATED}. System property: {@code annotated}.
   *
   * @return the annotated flag.
   */
  @Getter( lazy = true )
  private final boolean releaseAnnotated = computeReleaseAnnotated();

  private boolean computeReleaseAnnotated() {
    return getBoolValue( "annotated", "PAPRIKA_RELEASE_ANNOTATED", "release.annotated", true );
  }

  /**
   * Use signed tag. Default value: {@code false}. Property name:
   * {@code release.signed}. Environment variable:
   * {@code PAPRIKA_RELEASE_SIGNED}. System property: {@code signed}.
   *
   * @return the signed flag.
   */
  @Getter( lazy = true )
  private final boolean releaseSigned = computeReleaseSigned();

  private boolean computeReleaseSigned() {
    return getBoolValue( "signed", "PAPRIKA_RELEASE_SIGNED", "release.signed", false );
  }

  /**
   * Release message. Default value:
   * {@code "Release &#123;&#123;artifactId&#125;&#125; &#123;&#123;version&#125;&#125;"}.
   * Property name: {@code release.message}. Environment variable:
   * {@code PAPRIKA_RELEASE_MESSAGE}. System property: {@code message}.
   *
   * @return the release message.
   */
  @Getter( lazy = true )
  private final String releaseMessage = computeReleaseMessage();

  private String computeReleaseMessage() {
    return getConfigValue(
        "message",
        "PAPRIKA_RELEASE_MESSAGE",
        "release.message",
        "Release {{artifactId}} {{version}}" );
  }

  /**
   * Ignored flag. Default value: {@code false}. Property name:
   * {@code release.ignored}.
   *
   * @return the ignored flag.
   */
  @Getter( lazy = true )
  private final boolean releaseIgnored = computeReleaseIgnored();

  private boolean computeReleaseIgnored() {
    return getBoolValue( null, null, "release.ignored", false );
  }

  /**
   * Seek for sub-modules. Default value: {@code true}. Property name:
   * {@code release.subModules}. Environment variable:
   * {@code PAPRIKA_RELEASE_SUBMODULES}. System property: {@code subModules}.
   *
   * @return the sub-modules flag.
   */
  @Getter( lazy = true )
  private final boolean releaseSubModules = computeReleaseSubModules();

  private boolean computeReleaseSubModules() {
    return getBoolValue( "subModules", "PAPRIKA_RELEASE_SUBMODULES", "release.subModules", true );
  }

  /**
   * Part of version to increment. Can be {@code MAJOR}, {@code MINOR} or
   * {@code PATCH}. Default value: {@code MINOR}. Property name:
   * {@code release.increment}. Environment variable:
   * {@code PAPRIKA_RELEASE_INCREMENT}. System property: {@code increment}
   *
   * @return the part of version to increment.
   **/
  @Getter( lazy = true )
  private final IncrementPart releaseIncrement = computeReleaseIncrement();

  private IncrementPart computeReleaseIncrement() {
    String value =
        getConfigValue( "increment", "PAPRIKA_RELEASE_INCREMENT", "release.increment", "MINOR" );
    return Enum.valueOf( IncrementPart.class, value.trim().toUpperCase() );
  }

  /**
   * Skip the module if the last commit is already tagged. Default value:
   * {@code true}. Property name: {@code release.skipTagged} Environment
   * variable: {@code PAPRIKA_RELEASE_SKIP_TAGGED} System property:
   * {@code skipTagged}
   *
   * @return skip the module if the last commit is already tagged.
   **/
  @Getter( lazy = true )
  private final boolean skipTagged = computeSkipTagged();

  private boolean computeSkipTagged() {
    return getBoolValue( "skipTagged", "PAPRIKA_RELEASE_SKIP_TAGGED", "release.skipTagged", true );
  }

  /**
   * Write the proposed commands in a file. Property name:
   * {@code release.output}. Environment variable:
   * {@code PAPRIKA_RELEASE_OUTPUT}. System property: {@code output}.
   *
   * @return the path of the output file.
   **/
  @Getter( lazy = true )
  private final Path releaseOutput = computeReleaseOutput();

  private Path computeReleaseOutput() {
    String value = getConfigValue( "output", "PAPRIKA_RELEASE_OUTPUT", "release.output", "" );
    if( value.isEmpty() )
      return null;
    return Path.of( value ).toAbsolutePath();
  }

  /**
   * Execute proposed commands. Default value: {@code false}. Environment
   * variable: {@code PAPRIKA_RELEASE_EXEC}. System property: {@code exec}.
   *
   * @return the execution flag.
   */
  @Getter( lazy = true )
  private final boolean releaseExec = computeReleaseExec();

  private boolean computeReleaseExec() {
    return getBoolValue( "exec", "PAPRIKA_RELEASE_EXEC", null, false );
  }

  /*
   * Changelog
   */

  /**
   * Changelog start (oldest commit, excluded). Can be a valid a Git reference
   * expression or a version number of the module. If not defined, the changelog
   * is done until the second most recent tag. Use {@code root} to generate the
   * changelog since the start of the Git history. Default value: {@code ""}.
   * Environment variable: {@code PAPRIKA_CHANGELOG_FROM}. System property:
   * {@code from}.
   *
   * @return the changelog start
   **/
  @Getter( lazy = true )
  private final String changelogFrom = computeChangelogFrom();

  private String computeChangelogFrom() {
    return getConfigValue( "from", "PAPRIKA_CHANGELOG_FROM", null, "" );
  }

  /**
   * Changelog end (newest commit, included). Can be a valid a Git reference
   * expression or a version number of the module. Default value: {@code HEAD}.
   * Environment variable: {@code PAPRIKA_CHANGELOG_TO}. System property:
   * {@code to}.
   *
   * @return the changelog end.
   */
  @Getter( lazy = true )
  private final String changelogTo = computeChangelogTo();

  private String computeChangelogTo() {
    return getConfigValue( "to", "PAPRIKA_CHANGELOG_TO", null, "HEAD" );
  }

  /**
   * Changelog output path. If empty, the changelog is displayed in the Maven
   * logs, in the standard output. Default value: {@code ""}. Property name:
   * {@code changelog.output}. Environment variable:
   * {@code PAPRIKA_CHANGELOG_OUTPUT}. System property: {@code output}.
   *
   * @return the changelog output.
   */
  @Getter( lazy = true )
  private final Path changelogOutput = computeChangelogOutput();

  private Path computeChangelogOutput() {
    String value = getConfigValue( "output", "PAPRIKA_CHANGELOG_OUTPUT", "changelog.output", "" );
    if( value.isEmpty() )
      return null;
    return Path.of( value ).toAbsolutePath();
  }

  /**
   * Changelog template. Default value:
   *
   * <pre>
   * {{#releases}}
   *
   * {{#released}}
   * # {{version}}{{#tagged}} ({{tag.when.ISO_LOCAL_DATE}}){{/}}
   * {{^}}
   * # Not released
   * {{/released}}
   *
   * {{#changes}}
   * {{#message.conventional}}
   *  * [{{shortId}}] {{message.type}}{{#message.breaking}}!{{/}}: {{message.description}}
   * {{^}}
   *  * [{{shortId}}] {{message.firstLine}}
   * {{/}}
   * {{^changes}}
   * _No change._
   * {{/changes}}
   * {{/releases}}
   * </pre>
   *
   * Property name: {@code changelog.template}. Environment variable
   * {@code PAPRIKA_CHANGELOG_TEMPLATE}. System property: {@code template}.
   *
   * @return the changelog template.
   **/
  @Getter( lazy = true )
  private final String changelogTemplate = computeChangelogTemplate();

  private static final String DEFAULT_CHANGELOG_TEMPLATE = String.join(
      "\n",
      "{{#releases}}",
      "",
      "{{#released}}",
      "# {{version}}{{#tagged}} ({{tag.when.ISO_LOCAL_DATE}}){{/}}",
      "{{^}}",
      "# Not released",
      "{{/released}}",
      "",
      "{{#changes}}",
      "{{#message.conventional}}",
      " * [{{shortId}}] {{message.type}}{{#message.breaking}}!{{/}}: {{message.description}}",
      "{{^}}",
      " * [{{shortId}}] {{message.firstLine}}",
      "{{/}}",
      "{{^changes}}",
      "_No change._",
      "{{/changes}}",
      "{{/releases}}" );

  private String computeChangelogTemplate() {
    return getConfigValue(
        "template",
        "PAPRIKA_CHANGELOG_TEMPLATE",
        "changelog.template",
        DEFAULT_CHANGELOG_TEMPLATE );
  }

  /**
   * Template escaper to use. Can be empty, {@code NONE}, {@code HTML} or
   * {@code JSON}. Default value: {@code NONE}. Property name:
   * {@code template.escaper}. Environment variable: {@code PAPRIKA_ESCAPER}.
   * System property: {@code escaper}.
   *
   * @return the escaper to use.
   */
  @Getter( lazy = true )
  private final Escaper escaper = computeEscaper();

  private Escaper computeEscaper() {
    String value = getConfigValue( "escaper", "PAPRIKA_ESCAPER", "template.escaper", "NONE" );
    if( value.isEmpty() )
      return Escaper.NONE;
    return Enum.valueOf( Escaper.class, value.trim().toUpperCase() );
  }

  /**
   * Extact partial template. If {@code <name>} is the name of the partial, the
   * partial can be load from the property {@code template.partial.<name>}, or
   * for the environment variable {@code PAPRIKA_PARTIAL_<NAME>} (where
   * {@code <NAME>} is {@code name} in upper case), or from the system property
   * {@code <name>}.
   *
   * @param name the name of the partial template.
   * @return the partial template.
   */
  public String getPartialTemplate( String name ) {

    String partial = null;
    if( partial == null )
      partial = System.getProperty( name );
    if( partial == null )
      partial = System.getenv( "PAPRIKA_PARTIAL_" + name.toUpperCase().replaceAll( "\\W", "_" ) );
    if( partial == null )
      partial = configProperties.get( dir ).get( "template.partial." + name );

    return partial == null ? "" : partial;

  }

}
