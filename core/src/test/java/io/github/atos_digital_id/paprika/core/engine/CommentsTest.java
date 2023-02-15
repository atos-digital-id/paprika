package io.github.atos_digital_id.paprika.core.engine;

import org.junit.jupiter.api.Test;

/**
 * Comment tags represent content that should never appear in the resulting
 * output.
 *
 * The tag's content may contain any substring (including newlines) EXCEPT the
 * closing delimiter.
 *
 * Comment tags SHOULD be treated as standalone when appropriate.
 */
public class CommentsTest {

  /*
   * Comment blocks should be removed from the template.
   */
  @Test
  public void inline() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "12345{{! Comment Block! }}67890" );
      builder.expectedJoin( "1234567890" );
    } );

  }

  /*
   * Multiline comments should be permitted.
   */
  @Test
  public void multiline() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "12345{{!", "  This is a", "  multi-line comment...", "}}67890" );
      builder.expectedJoin( "1234567890" );
    } );

  }

  /*
   * All standalone comment lines should be removed.
   */
  @Test
  public void standalone() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "Begin.", "{{! Comment Block! }}", "End." );
      builder.expectedJoin( "Begin.", "End." );
    } );

  }

  /*
   * All standalone comment lines should be removed.
   */
  @Test
  public void indentedStandalone() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "Begin.", "  {{! Indented Comment Block! }}", "End." );
      builder.expectedJoin( "Begin.", "End." );
    } );

  }

  /*
   * "\r\n" should be considered a newline for standalone tags.
   */
  @Test
  public void standaloneLineEndings() {

    MustacheTest.test( builder -> {
      builder.template( "|\r\n{{! Standalone Comment }}\r\n|" );
      builder.expected( "|\r\n|" );
    } );

  }

  /*
   * Standalone tags should not require a newline to precede them.
   */
  @Test
  public void standaloneWithoutPreviousLine() {

    MustacheTest.test( builder -> {
      builder.template( "  {{! I'm Still Standalone }}\n!" );
      builder.expected( "!" );
    } );

  }

  /*
   * Standalone tags should not require a newline to follow them.
   */
  @Test
  public void standaloneWithoutNewline() {

    MustacheTest.test( builder -> {
      builder.template( "!\n  {{! I'm Still Standalone }}" );
      builder.expected( "!\n" );
    } );

  }

  /*
   * All standalone comment lines should be removed.
   */
  @Test
  public void multilineStandalone() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "Begin.", "{{!", "Something's going on here...", "}}", "End." );
      builder.expectedJoin( "Begin.", "End." );
    } );

  }

  /*
   * All standalone comment lines should be removed.
   */
  @Test
  public void indentedMultilineStandalone() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "Begin.", "{{!", "Something's going on here...", "}}", "End." );
      builder.expectedJoin( "Begin.", "End." );
    } );

  }

  /*
   * Inline comments should not strip whitespace
   */
  @Test
  public void indentedInline() {

    MustacheTest.test( builder -> {
      builder.template( "  12 {{! 34 }}\n" );
      builder.expected( "  12 \n" );
    } );

  }

  /*
   * Comment removal should preserve surrounding whitespace.
   */
  @Test
  public void surroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.template( "12345 {{! Comment Block! }} 67890" );
      builder.expected( "12345  67890" );
    } );

  }

  /*
   * Comments must never render, even if variable with same name exists.
   */
  @Test
  public void variableNameCollision() {

    MustacheTest.test( builder -> {
      builder.data( "! comment", 1 );
      builder.data( "! comment ", 2 );
      builder.data( "!comment", 3 );
      builder.data( "comment", 4 );
      builder.template( "comments never show: >{{! comment }}<" );
      builder.expected( "comments never show: ><" );
    } );

  }

}
