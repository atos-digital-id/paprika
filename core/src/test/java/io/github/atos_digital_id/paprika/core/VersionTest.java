package io.github.atos_digital_id.paprika.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.github.atos_digital_id.paprika.version.Version;
import io.github.atos_digital_id.paprika.version.VersionParsingException;

public class VersionTest {

  @Test
  public void testParse() throws VersionParsingException {

    assertThat( Version.parse( "1.9.0" ) )
        .isEqualTo( new Version( 1, 9, 0, new String[] {}, new String[] {} ) );

    assertThat( Version.parse( "1.0.0-alpha" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] { "alpha" }, new String[] {} ) );
    assertThat( Version.parse( "1.0.0-alpha.1" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] { "alpha", "1" }, new String[] {} ) );
    assertThat( Version.parse( "1.0.0-0.3.7" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] { "0", "3", "7" }, new String[] {} ) );
    assertThat( Version.parse( "1.0.0-x.7.z.92" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] { "x", "7", "z", "92" }, new String[] {} ) );
    assertThat( Version.parse( "1.0.0-x-y-z.-" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] { "x-y-z", "-" }, new String[] {} ) );

    assertThat( Version.parse( "1.0.0-alpha+001" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] { "alpha" }, new String[] { "001" } ) );
    assertThat( Version.parse( "1.0.0+20130313144700" ) )
        .isEqualTo( new Version( 1, 0, 0, new String[] {}, new String[] { "20130313144700" } ) );
    assertThat( Version.parse( "1.0.0-beta+exp.sha.5114f85" ) ).isEqualTo(
        new Version( 1, 0, 0, new String[] { "beta" }, new String[] { "exp", "sha", "5114f85" } ) );
    assertThat( Version.parse( "1.0.0+21AF26D3--117B344092BD" ) ).isEqualTo(
        new Version( 1, 0, 0, new String[] {}, new String[] { "21AF26D3--117B344092BD" } ) );

  }

  @Test
  public void testCompareEquals() throws VersionParsingException {

    Version v = Version.parse( "1.0.0-alpha+001" );
    Version w = Version.parse( "1.0.0-alpha+20130313144700" );

    assertThat( v.compareTo( w ) ).as( v + " = " + w ).isZero();

  }

  private void testCompareOrder( String ... versions ) throws VersionParsingException {

    List<Version> list = new ArrayList<>( versions.length );
    for( String v : versions )
      list.add( Version.parse( v ) );

    int n = list.size();
    for( int i = 0; i < n - 1; i++ ) {

      Version v = list.get( i );
      assertThat( v.compareTo( v ) ).as( v + " = " + v ).isZero();

      for( int j = i + 1; j < n; j++ ) {
        Version w = list.get( j );
        assertThat( v.compareTo( w ) ).as( v + " > " + w ).isNegative();
        assertThat( w.compareTo( v ) ).as( w + " > " + v ).isPositive();
      }
    }

  }

  @Test
  public void testCompareOrder() throws VersionParsingException {

    testCompareOrder( "1.0.0", "2.0.0", "2.1.0", "2.1.1" );

    testCompareOrder( "1.0.0-alpha", "1.0.0" );

    testCompareOrder(
        "1.0.0-alpha",
        "1.0.0-alpha.1",
        "1.0.0-alpha.beta",
        "1.0.0-beta",
        "1.0.0-beta.2",
        "1.0.0-beta.11",
        "1.0.0-rc.1",
        "1.0.0" );

  }

}
