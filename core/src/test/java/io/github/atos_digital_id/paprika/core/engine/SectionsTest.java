package io.github.atos_digital_id.paprika.core.engine;

import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.simpleList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.emptyMap;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.simpleMap;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.singleMap;

import org.junit.jupiter.api.Test;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder;

/**
 * Section tags and End Section tags are used in combination to wrap a section
 * of the template for iteration.
 *
 * These tags' content MUST be a non-whitespace character sequence NOT
 * containing the current closing delimiter; each Section tag MUST be followed
 * by an End Section tag with the same content within the same section.
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
 * <ol>
 * If the data is not of a list type, it is coerced into a list as follows: if
 * the data is truthy (e.g. `!!data == true`), use a single-element list
 * containing the data, otherwise use an empty list.
 *
 * For each element in the data list, the element MUST be pushed onto the
 * context stack, the section MUST be rendered, and the element MUST be popped
 * off the context stack.
 *
 * Section and End Section tags SHOULD be treated as standalone when
 * appropriate.
 */
public class SectionsTest {

  /*
   * Truthy sections should have their contents rendered.
   */
  @Test
  public void truthy() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "\"{{#boolean}}This should be rendered.{{/boolean}}\"" );
      builder.expected( "\"This should be rendered.\"" );
    } );

  }

  /*
   * Falsey sections should have their contents omitted.
   */
  @Test
  public void falsey() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", false );
      builder.template( "\"{{#boolean}}This should not be rendered.{{/boolean}}\"" );
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
      builder.template( "\"{{#null}}This should not be rendered.{{/null}}\"" );
      builder.expected( "\"\"" );
    } );

  }

  /*
   * Objects and hashes should be pushed onto the context stack.
   */
  @Test
  public void context() {

    MustacheTest.test( builder -> {
      builder.data( "context", singleMap( "name", "Joe" ) );
      builder.template( "\"{{#context}}Hi {{name}}.{{/context}}\"" );
      builder.expected( "\"Hi Joe.\"" );
    } );

  }

  /*
   * Names missing in the current context are looked up in the stack.
   */
  @Test
  public void parentContexts() {

    MustacheTest.test( builder -> {
      builder.data( "a", "foo" );
      builder.data( "b", "wrong" );
      builder.data( "sec", singleMap( "b", "bar" ) );
      builder.data( "c", singleMap( "d", "baz" ) );
      builder.template( "\"{{#sec}}{{a}}, {{b}}, {{c.d}}{{/sec}}\"" );
      builder.expected( "\"foo, bar, baz\"" );
    } );

  }

  /*
   * Non-false sections have their value at the top of context, accessible as
   * {{.}} or through the parent context. This gives a simple way to display
   * content conditionally if a variable exists.
   */
  @Test
  public void variableTest() {

    MustacheTest.test( builder -> {
      builder.data( "foo", "bar" );
      builder.template( "\"{{#foo}}{{.}} is {{foo}}{{/foo}}\"" );
      builder.expected( "\"bar is bar\"" );
    } );

  }

  /*
   * All elements on the context stack should be accessible within lists.
   */
  @Test
  public void listContexts() {

    MustacheTest.test( builder -> {
      builder.data(
          "tops",
          simpleList(
              simpleMap()
                  .add( "tname", simpleMap().add( "upper", "A" ).add( "lower", "a" ).build() )
                  .add(
                      "middles",
                      simpleList(
                          simpleMap().add( "mname", "1" ).add(
                              "bottoms",
                              simpleList( singleMap( "bname", "x" ), singleMap( "bname", "y" ) ) )
                              .build() ) )
                  .build() ) );
      builder.template(
          "{{#tops}}{{#middles}}{{tname.lower}}{{mname}}.{{#bottoms}}{{tname.upper}}{{mname}}{{bname}}.{{/bottoms}}{{/middles}}{{/tops}}" );
      builder.expected( "a1.A1x.A1y." );
    } );

  }

  /*
   * All elements on the context stack should be accessible.
   */
  @Test
  public void deeplyNestedContexts() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "one", 1 ) );
      builder.data( "b", singleMap( "two", 2 ) );
      builder.data(
          "c",
          simpleMap().add( "three", 3 )
              .add( "d", simpleMap().add( "four", 4 ).add( "five", 5 ).build() ).build() );
      builder.templateJoin(
          "{{#a}}",
          "{{one}}",
          "{{#b}}",
          "{{one}}{{two}}{{one}}",
          "{{#c}}",
          "{{one}}{{two}}{{three}}{{two}}{{one}}",
          "{{#d}}",
          "{{one}}{{two}}{{three}}{{four}}{{three}}{{two}}{{one}}",
          "{{#five}}",
          "{{one}}{{two}}{{three}}{{four}}{{five}}{{four}}{{three}}{{two}}{{one}}",
          "{{one}}{{two}}{{three}}{{four}}{{.}}6{{.}}{{four}}{{three}}{{two}}{{one}}",
          "{{one}}{{two}}{{three}}{{four}}{{five}}{{four}}{{three}}{{two}}{{one}}",
          "{{/five}}",
          "{{one}}{{two}}{{three}}{{four}}{{three}}{{two}}{{one}}",
          "{{/d}}",
          "{{one}}{{two}}{{three}}{{two}}{{one}}",
          "{{/c}}",
          "{{one}}{{two}}{{one}}",
          "{{/b}}",
          "{{one}}",
          "{{/a}}" );
      builder.expectedJoin(
          "1",
          "121",
          "12321",
          "1234321",
          "123454321",
          "12345654321",
          "123454321",
          "1234321",
          "12321",
          "121",
          "1" );
    } );

  }

  /*
   * Lists should be iterated; list items should visit the context stack.
   */
  @Test
  public void list() {

    MustacheTest.test( builder -> {
      builder.data(
          "list",
          simpleList( singleMap( "item", 1 ), singleMap( "item", 2 ), singleMap( "item", 3 ) ) );
      builder.template( "\"{{#list}}{{item}}{{/list}}\"" );
      builder.expected( "\"123\"" );
    } );

  }

  /*
   * Empty lists should behave like falsey values.
   */
  @Test
  public void emptyList() {

    MustacheTest.test( builder -> {
      builder.data( "list", SimpleCustomListBuilder.emptyList() );
      builder.template( "\"{{#list}}Yay lists!{{/list}}\"" );
      builder.expected( "\"\"" );
    } );

  }

  /*
   * Multiple sections per template should be permitted.
   */
  @Test
  public void doubled() {

    MustacheTest.test( builder -> {
      builder.data( "bool", true );
      builder.data( "two", "second" );
      builder.templateJoin(
          "{{#bool}}",
          "* first",
          "{{/bool}}",
          "* {{two}}",
          "{{#bool}}",
          "* third",
          "{{/bool}}" );
      builder.expectedJoin( "* first", "* second", "* third" );
    } );

  }

  /*
   * Nested truthy sections should have their contents rendered.
   */
  @Test
  public void nestedTruthy() {

    MustacheTest.test( builder -> {
      builder.data( "bool", true );
      builder.template( "| A {{#bool}}B {{#bool}}C{{/bool}} D{{/bool}} E |" );
      builder.expected( "| A B C D E |" );
    } );

  }

  /*
   * Nested falsey sections should be omitted.
   */
  @Test
  public void nestedFalsey() {

    MustacheTest.test( builder -> {
      builder.data( "bool", false );
      builder.template( "| A {{#bool}}B {{#bool}}C{{/bool}} D{{/bool}} E |" );
      builder.expected( "| A  E |" );
    } );

  }

  /*
   * Failed context lookups should be considered falsey.
   */
  @Test
  public void contextMisses() {

    MustacheTest.test( builder -> {
      builder.template( "[{{#missing}}Found key 'missing'!{{/missing}}]" );
      builder.expected( "[]" );
    } );

  }

  // Implicit Iterators

  /*
   * Implicit iterators should directly interpolate strings.
   */
  @Test
  public void implicitIteratorString() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( "a", "b", "c", "d", "e" ) );
      builder.template( "\"{{#list}}({{.}}){{/list}}\"" );
      builder.expected( "\"(a)(b)(c)(d)(e)\"" );
    } );

  }

  /*
   * Implicit iterators should cast integers to strings and interpolate.
   */
  @Test
  public void implicitIteratorInteger() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3, 4, 5 ) );
      builder.template( "\"{{#list}}({{.}}){{/list}}\"" );
      builder.expected( "\"(1)(2)(3)(4)(5)\"" );
    } );

  }

  /*
   * Implicit iterators should cast decimals to strings and interpolate.
   */
  @Test
  public void implicitIteratorDecimal() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1.10, 2.20, 3.30, 4.40, 5.50 ) );
      builder.template( "\"{{#list}}({{.}}){{/list}}\"" );
      builder.expected( "\"(1.1)(2.2)(3.3)(4.4)(5.5)\"" );
    } );

  }

  /*
   * Implicit iterators should allow iterating over nested arrays.
   */
  @Test
  public void implicitIteratorArray() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( simpleList( 1, 2, 3 ), simpleList( "a", "b", "c" ) ) );
      builder.template( "\"{{#list}}({{#.}}{{.}}{{/.}}){{/list}}\"" );
      builder.expected( "\"(123)(abc)\"" );
    } );

  }

  // Dotted Names

  /*
   * Dotted names should be valid for Section tags.
   */
  @Test
  public void dottedNamesTruthy() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "b", singleMap( "c", true ) ) );
      builder.template( "\"{{#a.b.c}}Here{{/a.b.c}}\" == \"Here\"" );
      builder.expected( "\"Here\" == \"Here\"" );
    } );

  }

  /*
   * Dotted names should be valid for Section tags.
   */
  @Test
  public void dottedNamesFalsey() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "b", singleMap( "c", false ) ) );
      builder.template( "\"{{#a.b.c}}Here{{/a.b.c}}\" == \"\"" );
      builder.expected( "\"\" == \"\"" );
    } );

  }

  /*
   * Dotted names that cannot be resolved should be considered falsey.
   */
  @Test
  public void dottedNamesBrokenChains() {

    MustacheTest.test( builder -> {
      builder.data( "a", emptyMap() );
      builder.template( "\"{{#a.b.c}}Here{{/a.b.c}}\" == \"\"" );
      builder.expected( "\"\" == \"\"" );
    } );

  }

  // Whitespace Sensitivity

  /*
   * Sections should not alter surrounding whitespace.
   */
  @Test
  public void surroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( " | {{#boolean}}\t|\t{{/boolean}} | \n" );
      builder.expected( " | \t|\t | \n" );
    } );

  }

  /*
   * Sections should not alter internal whitespace.
   */
  @Test
  public void internalWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( " | {{#boolean}} {{! Important Whitespace }}\n {{/boolean}} | \n" );
      builder.expected( " |  \n  | \n" );
    } );

  }

  /*
   * Single-line sections should not alter surrounding whitespace.
   */
  @Test
  public void indentedInlineSections() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( " {{#boolean}}YES{{/boolean}}\n {{#boolean}}GOOD{{/boolean}}\n" );
      builder.expected( " YES\n GOOD\n" );
    } );

  }

  /*
   * Standalone lines should be removed from the template.
   */
  @Test
  public void standaloneLines() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.templateJoin( "| This Is", "{{#boolean}}", "|", "{{/boolean}}", "| A Line" );
      builder.expectedJoin( "| This Is", "|", "| A Line" );
    } );

  }

  /*
   * Indented standalone lines should be removed from the template.
   */
  @Test
  public void indentedStandaloneLines() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.templateJoin( "| This Is", "  {{#boolean}}", "|", "  {{/boolean}}", "| A Line" );
      builder.expectedJoin( "| This Is", "|", "| A Line" );
    } );

  }

  /*
   * "\r\n" should be considered a newline for standalone tags.
   */
  @Test
  public void standaloneLineEndings() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "|\r\n{{#boolean}}\r\n{{/boolean}}\r\n|" );
      builder.expected( "|\r\n|" );
    } );

  }

  /*
   * Standalone tags should not require a newline to precede them.
   */
  @Test
  public void standaloneWithoutPreviousLine() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "  {{#boolean}}\n#{{/boolean}}\n/" );
      builder.expected( "#\n/" );
    } );

  }

  /*
   * Standalone tags should not require a newline to follow them.
   */
  @Test
  public void standaloneWithoutNewline() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "#{{#boolean}}\n/\n  {{/boolean}}" );
      builder.expected( "#\n/\n" );
    } );

  }

  // Whitespace Insensitivity

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void padding() {

    MustacheTest.test( builder -> {
      builder.data( "boolean", true );
      builder.template( "|{{# boolean }}={{/ boolean }}|" );
      builder.expected( "|=|" );
    } );

  }

}
