package io.github.atos_digital_id.paprika.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.github.atos_digital_id.paprika.utils.Patterns;
import io.github.atos_digital_id.paprika.utils.Patterns.PathFilter;

public class PatternsTest {

  private void shouldMatch( String pattern, String ... matches ) {

    PathFilter p = Patterns.pathFilter( pattern );
    for( String match : matches )
      assertThat( p.complete( match ) ).as( pattern + " ~ " + match ).isTrue();

  }

  private void shouldNotMatch( String pattern, String ... matches ) {

    PathFilter p = Patterns.pathFilter( pattern );
    for( String match : matches )
      assertThat( p.complete( match ) ).as( pattern + " ~ " + match ).isFalse();

  }

  @Test
  public void testSimple() {
    shouldMatch( "master", "master" );
    shouldNotMatch( "master", "my-branch", "from-master", "master-004" );
  }

  @Test
  public void testWildcards() {
    shouldMatch( "*", "master", "mybranch", "anything" );
    shouldMatch( "[JIRA-???]*", "[JIRA-123]new-awesome-feature" );
    shouldNotMatch( "[JIRA-???]*", "master", "sneaky-branch" );
    shouldNotMatch( "*", "feature/my-feature" );
  }

  @Test
  public void testDoubleWildcards() {
    shouldMatch( "**", "master", "mybranch", "feature/my-feature" );
    shouldMatch( "feature/**", "feature/my-feature" );
  }

  @Test
  public void testSimpleOr() {
    shouldMatch( "master:dev", "master", "dev" );
    shouldNotMatch( "master:dev", "sneaky-branch", "undevelopped" );
  }

  @Test
  public void testNot() {
    shouldMatch( "**:!bugfix/*", "master", "feature/my-feature" );
    shouldNotMatch( "**:!bugfix/*", "bugfix/my-bugfix" );
  }

  @Test
  public void testWild() {
    shouldMatch(
        "[JIRA-???\\*]*:![JIRA-999\\*]*:v*.*.*-RC:master",
        "master",
        "[JIRA-123*]new-awesome-feature",
        "v1.2.3-RC" );
    shouldNotMatch(
        "[JIRA-???\\*]*:![JIRA-999\\*]*:v*.*.*-RC:master",
        "1.2.3-RC",
        "sneaky-branch",
        ":master",
        "v1.2.3-RC|master",
        "[JIRA-999*]experimental-feature" );
  }

}
