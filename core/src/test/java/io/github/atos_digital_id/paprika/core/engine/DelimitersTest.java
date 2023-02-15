package io.github.atos_digital_id.paprika.core.engine;

import org.junit.jupiter.api.Test;

/**
 * Set Delimiter tags are used to change the tag delimiters for all content
 * following the tag in the current compilation unit.
 *
 * The tag's content MUST be any two non-whitespace sequences (separated by
 * whitespace) EXCEPT an equals sign ('=') followed by the current closing
 * delimiter.
 *
 * Set Delimiter tags SHOULD be treated as standalone when appropriate.
 */
public class DelimitersTest {

  /*
   * The equals sign (used on both sides) should permit delimiter changes.
   */
  @Test
  public void PairBehavior() {

    MustacheTest.test( builder -> {
      builder.data( "text", "Hey!" );
      builder.template( "{{=<% %>=}}(<%text%>)" );
      builder.expected( "(Hey!)" );
    } );

  }

  /*
   * Characters with special meaning regexen should be valid delimiters.
   */
  @Test
  public void SpecialCharacters() {

    MustacheTest.test( builder -> {
      builder.data( "text", "It worked!" );
      builder.template( "({{=[ ]=}}[text])" );
      builder.expected( "(It worked!)" );
    } );

  }

  /*
   * Delimiters set outside sections should persist.
   */
  @Test
  public void Sections() {

    MustacheTest.test( builder -> {
      builder.data( "section", true );
      builder.data( "data", "I got interpolated." );
      builder.templateJoin(
          "[",
          "{{#section}}",
          "  {{data}}",
          "  |data|",
          "{{/section}}",
          "",
          "{{= | | =}}",
          "|#section|",
          "  {{data}}",
          "  |data|",
          "|/section|",
          "]" );
      builder.expectedJoin(
          "[",
          "  I got interpolated.",
          "  |data|",
          "",
          "  {{data}}",
          "  I got interpolated.",
          "]" );
    } );

  }

  /*
   * Delimiters set outside inverted sections should persist.
   */
  @Test
  public void InvertedSections() {

    MustacheTest.test( builder -> {
      builder.data( "section", false );
      builder.data( "data", "I got interpolated." );
      builder.templateJoin(
          "[",
          "{{^section}}",
          "  {{data}}",
          "  |data|",
          "{{/section}}",
          "",
          "{{= | | =}}",
          "|^section|",
          "  {{data}}",
          "  |data|",
          "|/section|",
          "]" );
      builder.expectedJoin(
          "[",
          "  I got interpolated.",
          "  |data|",
          "",
          "  {{data}}",
          "  I got interpolated.",
          "]" );
    } );

  }

  /*
   * Delimiters set in a parent template should not affect a partial.
   */
  @Test
  public void PartialInheritence() {

    MustacheTest.test( builder -> {
      builder.data( "value", "yes" );
      builder.partial( "include", ".{{value}}." );
      builder.templateJoin( "[ {{>include}} ]", "{{= | | =}}", "[ |>include| ]" );
      builder.expectedJoin( "[ .yes. ]", "[ .yes. ]" );
    } );

  }

  /*
   * Delimiters set in a partial should not affect the parent template.
   */
  @Test
  public void PostPartialBehavior() {

    MustacheTest.test( builder -> {
      builder.data( "value", "yes" );
      builder.partial( "include", ".{{value}}. {{= | | =}} .|value|." );
      builder.templateJoin( "[ {{>include}} ]", "[ .{{value}}.  .|value|. ]" );
      builder.expectedJoin( "[ .yes.  .yes. ]", "[ .yes.  .|value|. ]" );
    } );

  }

  // Whitespace Sensitivity

  /*
   * Surrounding whitespace should be left untouched.
   */
  @Test
  public void SurroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.template( "| {{=@ @=}} |" );
      builder.expected( "|  |" );
    } );

  }

  /*
   * Whitespace should be left untouched.
   */
  @Test
  public void OutlyingWhitespaceInline() {

    MustacheTest.test( builder -> {
      builder.template( " | {{=@ @=}}\n" );
      builder.expected( " | \n" );
    } );

  }

  /*
   * Standalone lines should be removed from the template.
   */
  @Test
  public void StandaloneTag() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "Begin.", "{{=@ @=}}", "End." );
      builder.expectedJoin( "Begin.", "End." );
    } );

  }

  /*
   * Indented standalone lines should be removed from the template.
   */
  @Test
  public void IndentedStandaloneTag() {

    MustacheTest.test( builder -> {
      builder.templateJoin( "Begin.", "  {{=@ @=}}", "End." );
      builder.expectedJoin( "Begin.", "End." );
    } );

  }

  /*
   * "\r\n" should be considered a newline for standalone tags.
   */
  @Test
  public void StandaloneLineEndings() {

    MustacheTest.test( builder -> {
      builder.template( "|\r\n{{= @ @ =}}\r\n|" );
      builder.expected( "|\r\n|" );
    } );

  }

  /*
   * Standalone tags should not require a newline to precede them.
   */
  @Test
  public void StandaloneWithoutPreviousLine() {

    MustacheTest.test( builder -> {
      builder.template( "  {{=@ @=}}\n=" );
      builder.expected( "=" );
    } );

  }

  /*
   * Standalone tags should not require a newline to follow them.
   */
  @Test
  public void StandaloneWithoutNewline() {

    MustacheTest.test( builder -> {
      builder.template( "=\n  {{=@ @=}}" );
      builder.expected( "=\n" );
    } );

  }

  // Whitespace Insensitivity

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void PairWithPadding() {

    MustacheTest.test( builder -> {
      builder.template( "|{{= @   @ =}}|" );
      builder.expected( "||" );

    } );

  }

}
