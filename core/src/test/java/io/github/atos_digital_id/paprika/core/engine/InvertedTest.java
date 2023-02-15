package io.github.atos_digital_id.paprika.core.engine;

import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.simpleList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.emptyMap;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.singleMap;

import org.junit.jupiter.api.Test;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder;

/**
 * Inverted Section tags and End Section tags are used in combination to wrap a
 * section of the template.
 *
 * These tags' content MUST be a non-whitespace character sequence NOT
 * containing the current closing delimiter; each Inverted Section tag MUST be
 * followed by an End Section tag with the same content within the same section.
 *
 * This tag's content names the data to replace the tag. Name resolution is as
 * follows:
 * <ol type="1">
 * <li>Split the name on periods; the first part is the name to resolve, any
 * remaining parts should be retained.</li>
 * <li>Walk the context stack from top to bottom, finding the first context that
 * is a) a hash containing the name as a key OR b) an object responding to a
 * method with the given name.</li>
 * <li>If the context is a hash, the data is the value associated with the
 * name.</li>
 * <li>If the context is an object and the method with the given name has an
 * arity of 1, the method SHOULD be called with a String containing the
 * unprocessed contents of the sections; the data is the value returned.</li>
 * <li>Otherwise, the data is the value returned by calling the method with the
 * given name.</li>
 * <li>If any name parts were retained in step 1, each should be resolved
 * against a context stack containing only the result from the former
 * resolution. If any part fails resolution, the result should be considered
 * falsey, and should interpolate as the empty string.</li>
 * </ol>
 * If the data is not of a list type, it is coerced into a list as follows: if
 * the data is truthy (e.g. `!!data == true`), use a single-element list
 * containing the data, otherwise use an empty list.
 *
 * This section MUST NOT be rendered unless the data list is empty.
 *
 * Inverted Section and End Section tags SHOULD be treated as standalone when
 * appropriate.
 */
public class InvertedTest {

  /*
   * Falsey sections should have their contents rendered.
   */
  @Test
  public void falsey() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( "\"{{^boolean}}This should be rendered.{{/boolean}}\"" );
      builder.expected( "\"This should be rendered.\"" );
    } );

  }

  /*
   * Truthy sections should have their contents omitted.
   */
  @Test
  public void truthy() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "\"{{^boolean}}This should not be rendered.{{/boolean}}\"" );
      builder.expected( "\"\"" );
    } );

  }

  /*
   * Null is falsey.
   */
  @Test
  public void nullIsFalsey() {

    MustacheTest.test( builder -> {
      builder.data( "null", null );
      builder.template( "\"{{^null}}This should be rendered.{{/null}}\"" );
      builder.expected( "\"This should be rendered.\"" );
    } );

  }

  /*
   * Objects and hashes should behave like truthy values.
   */
  @Test
  public void context() {

    MustacheTest.test( builder -> {
      builder.data( "context", singleMap( "name", "Joe" ) );
      builder.template( "\"{{^context}}Hi {{name}}.{{/context}}\"" );
      builder.expected( "\"\"" );
    } );

  }

  /*
   * Lists should behave like truthy values.
   */
  @Test
  public void list() {

    MustacheTest.test( builder -> {
      builder.data(
          "list",
          simpleList( singleMap( "n", 1 ), singleMap( "n", 2 ), singleMap( "n", 3 ) ) );
      builder.template( "\"{{^list}}{{n}}{{/list}}\"" );
      builder.expected( "\"\"" );
    } );

  }

  /*
   * Empty lists should behave like falsey values.
   */
  @Test
  public void emptyList() {

    MustacheTest.test( builder -> {
      builder.data( "list", SimpleCustomListBuilder.emptyList() );
      builder.template( "\"{{^list}}Yay lists!{{/list}}\"" );
      builder.expected( "\"Yay lists!\"" );
    } );

  }

  /*
   * Multiple inverted sections per template should be permitted.
   */
  @Test
  public void doubled() {

    MustacheTest.test( builder -> {
      builder.data( "bool", false );
      builder.data( "two", "second" );
      builder.templateJoin(
          "{{^bool}}",
          "* first",
          "{{/bool}}",
          "* {{two}}",
          "{{^bool}}",
          "* third",
          "{{/bool}}" );
      builder.expectedJoin( "* first", "* second", "* third" );
    } );

  }

  /*
   * Nested falsey sections should have their contents rendered.
   */
  @Test
  public void nestedFalsey() {

    MustacheTest.test( builder -> {
      builder.data( "bool", false );
      builder.template( "| A {{^bool}}B {{^bool}}C{{/bool}} D{{/bool}} E |" );
      builder.expected( "| A B C D E |" );
    } );

  }

  /*
   * Nested truthy sections should be omitted.
   */
  @Test
  public void nestedTruthy() {

    MustacheTest.test( builder -> {
      builder.data( "bool", true );
      builder.template( "| A {{^bool}}B {{^bool}}C{{/bool}} D{{/bool}} E |" );
      builder.expected( "| A  E |" );
    } );

  }

  /*
   * Failed context lookups should be considered falsey.
   */
  @Test
  public void contextMisses() {

    MustacheTest.test( builder -> {
      builder.template( "[{{^missing}}Cannot find key 'missing'!{{/missing}}]" );
      builder.expected( "[Cannot find key 'missing'!]" );
    } );

  }

  // Dotted Names

  /*
   * Dotted names should be valid for Inverted Section tags.
   */
  @Test
  public void dottedNamesTruthy() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "b", singleMap( "c", true ) ) );
      builder.template( "\"{{^a.b.c}}Not Here{{/a.b.c}}\" == \"\"" );
      builder.expected( "\"\" == \"\"" );
    } );

  }

  /*
   * Dotted names should be valid for Inverted Section tags.
   */
  @Test
  public void dottedNamesFalsey() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "b", singleMap( "c", false ) ) );
      builder.template( "\"{{^a.b.c}}Not Here{{/a.b.c}}\" == \"Not Here\"" );
      builder.expected( "\"Not Here\" == \"Not Here\"" );
    } );

  }

  /*
   * Dotted names that cannot be resolved should be considered falsey.
   */
  @Test
  public void dottedNamesBrokenChains() {

    MustacheTest.test( builder -> {
      builder.data( "a", emptyMap() );
      builder.template( "\"{{^a.b.c}}Not Here{{/a.b.c}}\" == \"Not Here\"" );
      builder.expected( "\"Not Here\" == \"Not Here\"" );
    } );

  }

  // Whitespace Sensitivity

  /*
   * Inverted sections should not alter surrounding whitespace.
   */
  @Test
  public void surroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( " | {{^boolean}}\t|\t{{/boolean}} | \n" );
      builder.expected( " | \t|\t | \n" );
    } );

  }

  /*
   * Inverted should not alter internal whitespace.
   */
  @Test
  public void internalWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( " | {{^boolean}} {{! Important Whitespace }}\n {{/boolean}} | \n" );
      builder.expected( " |  \n  | \n" );
    } );

  }

  /*
   * Single-line sections should not alter surrounding whitespace.
   */
  @Test
  public void indentedInlineSections() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( " {{^boolean}}NO{{/boolean}}\n {{^boolean}}WAY{{/boolean}}\n" );
      builder.expected( " NO\n WAY\n" );
    } );

  }

  /*
   * Standalone lines should be removed from the template.
   */
  @Test
  public void standaloneLines() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.templateJoin( "| This Is", "{{^boolean}}", "|", "{{/boolean}}", "| A Line" );
      builder.expectedJoin( "| This Is", "|", "| A Line" );
    } );

  }

  /*
   * Standalone indented lines should be removed from the template.
   */
  @Test
  public void standaloneIndentedLines() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.templateJoin( "| This Is", "  {{^boolean}}", "|", "  {{/boolean}}", "| A Line" );
      builder.expectedJoin( "| This Is", "|", "| A Line" );
    } );

  }

  /*
   * "\r\n" should be considered a newline for standalone tags.
   */
  @Test
  public void standaloneLineEndings() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( "|\r\n{{^boolean}}\r\n{{/boolean}}\r\n|" );
      builder.expected( "|\r\n|" );
    } );

  }

  /*
   * Standalone tags should not require a newline to precede them.
   */
  @Test
  public void standaloneWithoutPreviousLine() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( "  {{^boolean}}\n^{{/boolean}}\n/" );
      builder.expected( "^\n/" );
    } );

  }

  /*
   * Standalone tags should not require a newline to follow them.
   */
  @Test
  public void standaloneWithoutNewline() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( "^{{^boolean}}\n/\n  {{/boolean}}" );
      builder.expected( "^\n/\n" );
    } );

  }

  // Whitespace Insensitivity

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void padding() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( "|{{^ boolean }}={{/ boolean }}|" );
      builder.expected( "|=|" );
    } );

  }

}
