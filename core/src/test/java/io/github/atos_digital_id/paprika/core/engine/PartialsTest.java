package io.github.atos_digital_id.paprika.core.engine;

import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.emptyList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.simpleList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.simpleMap;

import org.junit.jupiter.api.Test;

/*
 * Partial tags are used to expand an external template into the current
 * template.
 *
 * The tag's content MUST be a non-whitespace character sequence NOT containing
 * the current closing delimiter.
 *
 * This tag's content names the partial to inject.  Set Delimiter tags MUST NOT
 * affect the parsing of a partial.  The partial MUST be rendered against the
 * context stack local to the tag.  If the named partial cannot be found, the
 * empty string SHOULD be used instead, as in interpolations.
 *
 * Partial tags SHOULD be treated as standalone when appropriate.  If this tag
 * is used standalone, any whitespace preceding the tag should treated as
 * indentation, and prepended to each line of the partial before rendering.
 */
public class PartialsTest {

  /*
   * The greater-than operator should expand to the named partial.
   */
  @Test
  public void basicBehavior() {

    MustacheTest.test( builder -> {
      builder.template( "\"{{>text}}\"" );
      builder.partial( "text", "from partial" );
      builder.expected( "\"from partial\"" );
    } );

  }

  /*
   * The empty string should be used when the named partial is not found.
   */
  @Test
  public void failedLookup() {

    MustacheTest.test( builder -> {
      builder.template( "\"{{>text}}\"" );
      builder.expected( "\"\"" );
    } );

  }

  /*
   * The greater-than operator should operate within the current context.
   */
  @Test
  public void context() {

    MustacheTest.test( builder -> {
      builder.data( "text", "content" );
      builder.template( "\"{{>partial}}\"" );
      builder.partial( "partial", "*{{text}}*" );
      builder.expected( "\"*content*\"" );
    } );

  }

  /*
   * The greater-than operator should properly recurse.
   */
  @Test
  public void recursion() {

    MustacheTest.test( builder -> {
      builder.data( "content", "X" );
      builder.data(
          "nodes",
          simpleList( simpleMap().add( "content", "Y" ).add( "nodes", emptyList() ).build() ) );
      builder.template( "{{>node}}" );
      builder.partial( "node", "{{content}}<{{#nodes}}{{>node}}{{/nodes}}>" );
      builder.expected( "X<Y<>>" );
    } );

  }

  // Whitespace Sensitivity

  /*
   * The greater-than operator should not alter surrounding whitespace.
   */
  @Test
  public void surroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.template( "| {{>partial}} |" );
      builder.partial( "partial", "\t|\t" );
      builder.expected( "| \t|\t |" );
    } );

  }

  /*
   * Whitespace should be left untouched.
   */
  @Test
  public void inlineIndentation() {

    MustacheTest.test( builder -> {
      builder.data( "data", "|" );
      builder.template( "  {{data}}  {{> partial}}\n" );
      builder.partial( "partial", ">\n>" );
      builder.expected( "  |  >\n>\n" );
    } );

  }

  /*
   * "\r\n" should be considered a newline for standalone tags.
   */
  @Test
  public void standaloneLineEndings() {

    MustacheTest.test( builder -> {
      builder.template( "|\r\n{{>partial}}\r\n|" );
      builder.partial( "partial", ">" );
      builder.expected( "|\r\n>|" );
    } );

  }

  /*
   * Standalone tags should not require a newline to precede them.
   */
  @Test
  public void standaloneWithoutPreviousLine() {

    MustacheTest.test( builder -> {
      builder.template( "  {{>partial}}\n>" );
      builder.partial( "partial", ">\n>" );
      builder.expected( "  >\n  >>" );
    } );

  }

  /*
   * Standalone tags should not require a newline to follow them.
   */
  @Test
  public void standaloneWithoutNewline() {

    MustacheTest.test( builder -> {
      builder.template( ">\n  {{>partial}}" );
      builder.partial( "partial", ">\n>" );
      builder.expected( ">\n  >\n  >" );
    } );

  }

  /*
   * Each line of the partial should be indented before rendering.
   */
  @Test
  public void standaloneIndentation() {

    MustacheTest.test( builder -> {
      builder.data( "content", "<\n->" );
      builder.templateJoin( "\\", " {{>partial}}", "/" );
      builder.partialJoin( "partial", "|", "{{{content}}}", "|" );
      builder.expectedJoin( "\\", " |", " <", "->", " |", "/" );
    } );

  }

  // Whitespace Insensitivity

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void paddingWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "|{{> partial }}|" );
      builder.partial( "partial", "[]" );
      builder.expected( "|[]|" );
    } );

  }

}
