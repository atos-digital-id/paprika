package io.github.atos_digital_id.paprika.version;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.atos_digital_id.paprika.utils.Patterns;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;

/**
 * Implementation of a version with respect of
 * <a href="https://semver.org/" target="_top">Semantic Versionning</a>.
 **/
@Data
public class Version implements Comparable<Version> {

  /**
   * {@code "SNAPSHOT"}.
   **/
  public static final String SNAPSHOT = "SNAPSHOT";

  /**
   * {@code "ILLEGAL"}.
   **/
  public static final String ILLEGAL = "ILLEGAL";

  /**
   * {@code "WRONG-TAG"}.
   **/
  public static final String WRONG_TAG = "WRONG-TAG";

  /**
   * Empty array of strings.
   **/
  public static final String[] EMPTY_STRINGS = new String[0];

  /**
   * Major part.
   *
   * @return the major part.
   **/
  private final int major;

  /**
   * Minor part.
   *
   * @return the minor part.
   **/
  private final int minor;

  /**
   * Patch part.
   *
   * @return the patch part.
   **/
  private final int patch;

  /**
   * Prerelease tags.
   *
   * @return the prerelease tags.
   **/
  @NonNull
  private final String[] prereleases;

  /**
   * Build tags.
   *
   * @return the build tags.
   **/
  @NonNull
  private final String[] builds;

  /**
   * Check if the version contains {@code SNAPSHOT} in the prereleases tags.
   *
   * @return {@code true} if the version is a SNAPSHOT
   **/
  @Getter( lazy = true )
  @EqualsAndHashCode.Exclude
  private final boolean snapshot = testSnapshot();

  private boolean testSnapshot() {
    return Arrays.asList( prereleases ).contains( SNAPSHOT );
  }

  /**
   * String representation of the version.
   *
   * @return the string representation.
   **/
  @Getter( lazy = true )
  @EqualsAndHashCode.Exclude
  private final String string = computeString();

  private String computeString() {

    StringBuilder builder = new StringBuilder().append( major ).append( "." ).append( minor )
        .append( "." ).append( patch );

    if( prereleases.length > 0 ) {
      builder.append( "-" ).append( prereleases[0] );
      for( int i = 1; i < prereleases.length; i++ )
        builder.append( "." ).append( prereleases[i] );
    }

    if( builds.length > 0 ) {
      builder.append( "+" ).append( builds[0] );
      for( int i = 1; i < builds.length; i++ )
        builder.append( "." ).append( builds[i] );
    }

    return builder.toString();

  }

  @Override
  public String toString() {
    return this.getString();
  }

  @Override
  public int compareTo( @NonNull Version v ) {

    if( this.major < v.major )
      return -1;
    if( this.major > v.major )
      return 1;

    if( this.minor < v.minor )
      return -1;
    if( this.minor > v.minor )
      return 1;

    if( this.patch < v.patch )
      return -1;
    if( this.patch > v.patch )
      return 1;

    int l = this.prereleases.length;
    int n = v.prereleases.length;

    boolean p = l == 0;
    boolean q = n == 0;
    if( p && q )
      return 0;
    if( !p && q )
      return -1;
    if( p && !q )
      return 1;

    Integer a, b, c;
    int i = 0;
    while( true ) {

      p = i == l;
      q = i == n;
      if( p && q )
        return 0;
      if( !p && q )
        return 1;
      if( p && !q )
        return -1;

      a = castAsNumber( this.prereleases[i] );
      b = castAsNumber( v.prereleases[i] );

      p = a != null;
      q = b != null;

      if( p && q && ( c = Integer.compare( a, b ) ) != 0 )
        return c;
      if( !p && q )
        return 1;
      if( p && !q )
        return -1;

      if( ( c = this.prereleases[i].compareTo( v.prereleases[i] ) ) != 0 )
        return Integer.signum( c );

      i++;

    }

  }

  private Integer castAsNumber( String number ) {

    int value = 0;

    for( int i = 0; i < number.length(); i++ ) {
      char c = number.charAt( i );
      if( c < '0' || '9' < c )
        return null;
      value = ( 10 * value ) + ( c - '0' );
    }

    return value;

  }

  private static final Pattern PATTERN = Pattern.compile(
      "^(?<major>0|[1-9]\\d*)\\.(?<minor>0|[1-9]\\d*)\\.(?<patch>0|[1-9]\\d*)"
          + "(?:-(?<prerelease>(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)"
          + "(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?"
          + "(?:\\+(?<buildmetadata>[0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$" );

  /**
   * Parse a version.
   *
   * @param version the version to parse.
   * @return the parsed version.
   **/
  public static Version parse( @NonNull String version ) {

    Matcher matcher = PATTERN.matcher( version );
    if( matcher.matches() ) {

      int major = Integer.parseInt( matcher.group( "major" ) );
      int minor = Integer.parseInt( matcher.group( "minor" ) );
      int patch = Integer.parseInt( matcher.group( "patch" ) );
      String[] prereleases =
          Patterns.split( matcher.group( "prerelease" ), '.' ).toArray( EMPTY_STRINGS );
      String[] builds =
          Patterns.split( matcher.group( "buildmetadata" ), '.' ).toArray( EMPTY_STRINGS );

      return new Version( major, minor, patch, prereleases, builds );

    } else {

      return new Version( 0, 0, 0, new String[] { ILLEGAL }, new String[] { version } );

    }

  }

}
