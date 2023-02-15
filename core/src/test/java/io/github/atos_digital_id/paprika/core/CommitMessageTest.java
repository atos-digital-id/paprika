package io.github.atos_digital_id.paprika.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.function.Consumer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import io.github.atos_digital_id.paprika.utils.templating.value.CommitMessageValue;
import io.github.atos_digital_id.paprika.utils.templating.value.CommitMessageValue.CommitMessageValueBuilder;

public class CommitMessageTest {

  private void testParseMessage( Consumer<CommitMessageValueBuilder> setter, String ... lines ) {

    String msg = String.join( "\n", lines );

    CommitMessageValueBuilder builder = CommitMessageValue.builder();
    builder.full( msg );
    builder.firstLine( lines[0] );
    setter.accept( builder );
    CommitMessageValue expected = builder.build();

    assertThat( CommitMessageValue.wrap( msg ) ).usingRecursiveComparison().isEqualTo( expected );

  }

  /*
   * Conventional commits examples
   */

  @Test
  @DisplayName( "Commit message with description and breaking change footer" )
  public void conventional01() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "feat" );
      builder.scopes( "" );
      builder.description( "allow provided config object to extend other configs" );
      builder.body( "" );
      builder.footerBuilder()
          .add(
              "BREAKING-CHANGE",
              "`extends` key in config file is now used for extending other config files" )
          .build();
    },
        "feat: allow provided config object to extend other configs",
        "",
        "BREAKING CHANGE: `extends` key in config file is now used for extending other config files" );

  }

  @Test
  @DisplayName( "Commit message with ! to draw attention to breaking change" )
  public void conventional02() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "feat" );
      builder.scopes( "" );
      builder.description( "send an email to the customer when a product is shipped" );
      builder.body( "" );
      builder.footerBuilder()
          .add( "BREAKING-CHANGE", "send an email to the customer when a product is shipped" )
          .build();
    }, "feat!: send an email to the customer when a product is shipped" );

  }

  @Test
  @DisplayName( "Commit message with scope and ! to draw attention to breaking change" )
  public void conventional03() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "feat" );
      builder.scopes( "api" );
      builder.description( "send an email to the customer when a product is shipped" );
      builder.body( "" );
      builder.footerBuilder()
          .add( "BREAKING-CHANGE", "send an email to the customer when a product is shipped" )
          .build();
    }, "feat(api)!: send an email to the customer when a product is shipped" );

  }

  @Test
  @DisplayName( "Commit message with both ! and BREAKING CHANGE footer" )
  public void conventional04() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "chore" );
      builder.scopes( "" );
      builder.description( "drop support for Node 6" );
      builder.body( "" );
      builder.footerBuilder()
          .add( "BREAKING-CHANGE", "use JavaScript features not available in Node 6." ).build();
    },
        "chore!: drop support for Node 6",
        "",
        "BREAKING CHANGE: use JavaScript features not available in Node 6." );

  }

  @Test
  @DisplayName( "Commit message with no body" )
  public void conventional05() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "docs" );
      builder.scopes( "" );
      builder.description( "correct spelling of CHANGELOG" );
      builder.body( "" );
    }, "docs: correct spelling of CHANGELOG" );

  }

  @Test
  @DisplayName( "Commit message with scope" )
  public void conventional06() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "feat" );
      builder.scopes( "lang" );
      builder.description( "add Polish language" );
      builder.body( "" );
    }, "feat(lang): add Polish language" );

  }

  @Test
  @DisplayName( "Commit message with multi-paragraph body and multiple footers" )
  public void conventional07() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "fix" );
      builder.scopes( "" );
      builder.description( "prevent racing of requests" );
      builder.multilineBody(
          "Introduce a request id and a reference to latest request. Dismiss",
          "incoming responses other than from latest request.",
          "",
          "Remove timeouts which were used to mitigate the racing issue but are",
          "obsolete now." );
      builder.footerBuilder().add( "REFS", "#123" ).add( "REVIEWED-BY", "Z" ).build();
    },
        "fix: prevent racing of requests",
        "",
        "Introduce a request id and a reference to latest request. Dismiss",
        "incoming responses other than from latest request.",
        "",
        "Remove timeouts which were used to mitigate the racing issue but are",
        "obsolete now.",
        "",
        "Reviewed-by: Z",
        "Refs: #123" );

  }

  /*
   * Extrem Conventional commits
   */

  @Test
  @DisplayName( "Commit message with several footers with same key" )
  public void xconventional01() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "fix" );
      builder.scopes( "" );
      builder.description( "prevent racing of requests" );
      builder.body( "" );
      builder.footerBuilder().add( "REFS", "#123", "#456" ).add( "REVIEWED-BY", "Z" ).build();
    }, "fix: prevent racing of requests", "", "Reviewed-by: Z", "Refs: #123", "Refs: #456" );

  }

  @Test
  @DisplayName( "Commit message with multiline footers" )
  public void xconventional02() {

    testParseMessage( builder -> {
      builder.isConventional( true );
      builder.type( "fix" );
      builder.scopes( "" );
      builder.description( "prevent racing of requests" );
      builder.body( "" );
      builder.footerBuilder().add( "REFS", "#123", "#456" ).add( "REVIEWED-BY", "Z" ).build();
    }, "fix: prevent racing of requests", "", "Reviewed-by: Z", "Refs: #123", "      #456" );

  }

  /*
   * Not conventional commits
   */

  @Test
  @DisplayName( "Unconventional commit with tag" )
  public void unconventional01() {

    testParseMessage( builder -> {
      builder.isConventional( false );
      builder.type( "" );
      builder.scopes( "" );
      builder.description( "" );
      builder.body( "Finally, repare that famous problem." );
      builder.footerBuilder().add( "REF", "[5]" ).build();
    },
        "Pull request #123: Repare that problem.",
        "",
        "Finally, repare that famous problem.",
        "",
        "Ref: [5]" );
  }

}
