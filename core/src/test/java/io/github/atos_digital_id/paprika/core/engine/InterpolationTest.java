package io.github.atos_digital_id.paprika.core.engine;

import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.emptyMap;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.singleMap;

import org.junit.jupiter.api.Test;

/**
 * Interpolation tags are used to integrate dynamic content into the template.
 *
 * The tag's content MUST be a non-whitespace character sequence NOT containing
 * the current closing delimiter.
 *
 * This tag's content names the data to replace the tag. A single period (`.`)
 * indicates that the item currently sitting atop the context stack should be
 * used; otherwise, name resolution is as follows:
 * <ol type="1">
 * <li>Split the name on periods; the first part is the name to resolve, any
 * remaining parts should be retained</li>
 * <li>Walk the context stack from top to bottom, finding the first context that
 * is a) a hash containing the name as a key OR b) an object responding to a
 * method with the given name.</li>
 * <li>If the context is a hash, the data is the value associated with the
 * name.</li>
 * <li>If the context is an object, the data is the value returned by the method
 * with the given name.</li>
 * <li>If any name parts were retained in step 1, each should be resolved
 * against a context stack containing only the result from the former
 * resolution. If any part fails resolution, the result should be considered
 * falsey, and should interpolate as the empty string.
 * <li>
 * </ol>
 * Data should be coerced into a string (and escaped, if appropriate) before
 * interpolation.
 *
 * The Interpolation tags MUST NOT be treated as standalone.
 */
public class InterpolationTest {

  /*
   * Mustache-free templates should render as-is.
   */
  @Test
  public void noInterpolation() {

    MustacheTest.test( builder -> {
      builder.template( "Hello from {Mustache}!" );
      builder.expected( "Hello from {Mustache}!" );
    } );

  }

  /*
   * Unadorned tags should interpolate content into the template.
   */
  @Test
  public void basicInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "world" );
      builder.template( "Hello, {{subject}}!" );
      builder.expected( "Hello, world!" );
    } );

  }

  /*
   * Basic interpolation should be HTML escaped.
   */
  @Test
  public void htmlEscaping() {

    MustacheTest.test( builder -> {
      builder.data( "forbidden", "& \" < >" );
      builder.template( "These characters should be HTML escaped: {{forbidden}}" );
      builder.expected( "These characters should be HTML escaped: &amp; &quot; &lt; &gt;" );
    } );

  }

  /*
   * Triple mustaches should interpolate without HTML escaping.
   */
  @Test
  public void tripleMustache() {

    MustacheTest.test( builder -> {
      builder.data( "forbidden", "& \" < >" );
      builder.template( "These characters should not be HTML escaped: {{{forbidden}}}" );
      builder.expected( "These characters should not be HTML escaped: & \" < >" );
    } );

  }

  /*
   * Ampersand should interpolate without HTML escaping.
   */
  @Test
  public void ampersand() {

    MustacheTest.test( builder -> {
      builder.data( "forbidden", "& \" < >" );
      builder.template( "These characters should not be HTML escaped: {{&forbidden}}" );
      builder.expected( "These characters should not be HTML escaped: & \" < >" );
    } );

  }

  /*
   * Integers should interpolate seamlessly.
   */
  @Test
  public void basicIntegerInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "mph", 85 );
      builder.template( "\"{{mph}} miles an hour!\"" );
      builder.expected( "\"85 miles an hour!\"" );
    } );

  }

  /*
   * Integers should interpolate seamlessly.
   */
  @Test
  public void tripleMustacheIntegerInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "mph", 85 );
      builder.template( "\"{{{mph}}} miles an hour!\"" );
      builder.expected( "\"85 miles an hour!\"" );
    } );

  }

  /*
   * Integers should interpolate seamlessly.
   */
  @Test
  public void ampersandIntegerInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "mph", 85 );
      builder.template( "\"{{&mph}} miles an hour!\"" );
      builder.expected( "\"85 miles an hour!\"" );
    } );

  }

  /*
   * Decimals should interpolate seamlessly with proper significance.
   */
  @Test
  public void basicDecimalInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "power", 1.210 );
      builder.template( "\"{{power}} jiggawatts!\"" );
      builder.expected( "\"1.21 jiggawatts!\"" );
    } );

  }

  /*
   * Decimals should interpolate seamlessly with proper significance.
   */
  @Test
  public void tripleMustacheDecimalInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "power", 1.210 );
      builder.template( "\"{{{power}}} jiggawatts!\"" );
      builder.expected( "\"1.21 jiggawatts!\"" );
    } );

  }

  /*
   * Decimals should interpolate seamlessly with proper significance.
   */
  @Test
  public void ampersandDecimalInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "power", 1.210 );
      builder.template( "\"{{&power}} jiggawatts!\"" );
      builder.expected( "\"1.21 jiggawatts!\"" );
    } );

  }

  /*
   * Nulls should interpolate as the empty string.
   */
  @Test
  public void basicNullInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "cannot", null );
      builder.template( "I ({{cannot}}) be seen!" );
      builder.expected( "I () be seen!" );
    } );

  }

  /*
   * Nulls should interpolate as the empty string.
   */
  @Test
  public void tripleMustacheNullInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "cannot", null );
      builder.template( "I ({{{cannot}}}) be seen!" );
      builder.expected( "I () be seen!" );
    } );

  }

  /*
   * Nulls should interpolate as the empty string.
   */
  @Test
  public void ampersandNullInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "cannot", null );
      builder.template( "I ({{&cannot}}) be seen!" );
      builder.expected( "I () be seen!" );
    } );

  }

  // Context Misses

  /*
   * Failed context lookups should default to empty strings.
   */
  @Test
  public void basicContextMissInterpolation() {

    MustacheTest.test( builder -> {
      builder.template( "I ({{cannot}}) be seen!" );
      builder.expected( "I () be seen!" );
    } );

  }

  /*
   * Failed context lookups should default to empty strings.
   */
  @Test
  public void tripleMustacheContextMissInterpolation() {

    MustacheTest.test( builder -> {
      builder.template( "I ({{{cannot}}}) be seen!" );
      builder.expected( "I () be seen!" );
    } );

  }

  /*
   * Failed context lookups should default to empty strings.
   */
  @Test
  public void ampersandContextMissInterpolation() {

    MustacheTest.test( builder -> {
      builder.template( "I ({{&cannot}}) be seen!" );
      builder.expected( "I () be seen!" );
    } );

  }

  // Dotted Names

  /*
   * Dotted names should be considered a form of shorthand for sections.
   */
  @Test
  public void dottedNamesBasicInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "person", singleMap( "name", "Joe" ) );
      builder.template( "\"{{person.name}}\" == \"{{#person}}{{name}}{{/person}}\"" );
      builder.expected( "\"Joe\" == \"Joe\"" );
    } );

  }

  /*
   * Dotted names should be considered a form of shorthand for sections.
   */
  @Test
  public void dottedNamesTripleMustacheInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "person", singleMap( "name", "Joe" ) );
      builder.template( "\"{{{person.name}}}\" == \"{{#person}}{{name}}{{/person}}\"" );
      builder.expected( "\"Joe\" == \"Joe\"" );
    } );

  }

  /*
   * Dotted names should be considered a form of shorthand for sections.
   */
  @Test
  public void dottedNamesAmpersandInterpolation() {

    MustacheTest.test( builder -> {
      builder.data( "person", singleMap( "name", "Joe" ) );
      builder.template( "\"{{&person.name}}\" == \"{{#person}}{{name}}{{/person}}\"" );
      builder.expected( "\"Joe\" == \"Joe\"" );
    } );

  }

  /*
   * Dotted names should be functional to any level of nesting.
   */
  @Test
  public void dottedNamesArbitraryDepth() {

    MustacheTest.test( builder -> {
      builder.data(
          "a",
          singleMap(
              "b",
              singleMap( "c", singleMap( "d", singleMap( "e", singleMap( "name", "Phil" ) ) ) ) ) );
      builder.template( "\"{{a.b.c.d.e.name}}\" == \"Phil\"" );
      builder.expected( "\"Phil\" == \"Phil\"" );
    } );

  }

  /*
   * Any falsey value prior to the last part of the name should yield ''.
   */
  @Test
  public void dottedNamesBrokenChains() {

    MustacheTest.test( builder -> {
      builder.data( "a", emptyMap() );
      builder.template( "\"{{a.b.c}}\" == \"\"" );
      builder.expected( "\"\" == \"\"" );
    } );

  }

  /*
   * Each part of a dotted name should resolve only against its parent.
   */
  @Test
  public void dottedNamesBrokenChainResolution() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "b", emptyMap() ) );
      builder.data( "c", singleMap( "name", "Jim" ) );
      builder.template( "\"{{a.b.c.name}}\" == \"\"" );
      builder.expected( "\"\" == \"\"" );
    } );

  }

  /*
   * The first part of a dotted name should resolve as any other name.
   */
  @Test
  public void dottedNamesInitialResolution() {

    MustacheTest.test( builder -> {
      builder.data(
          "a",
          singleMap(
              "b",
              singleMap( "c", singleMap( "d", singleMap( "e", singleMap( "name", "Phil" ) ) ) ) ) );
      builder.data(
          "b",
          singleMap( "c", singleMap( "d", singleMap( "e", singleMap( "name", "Wrong" ) ) ) ) );
      builder.template( "\"{{#a}}{{b.c.d.e.name}}{{/a}}\" == \"Phil\"" );
      builder.expected( "\"Phil\" == \"Phil\"" );
    } );

  }

  /*
   * Dotted names should be resolved against former resolutions.
   */
  @Test
  public void dottedNamesContextPrecedence() {

    MustacheTest.test( builder -> {
      builder.data( "a", singleMap( "b", emptyMap() ) );
      builder.data( "b", singleMap( "c", "ERROR" ) );
      builder.template( "{{#a}}{{b.c}}{{/a}}" );
      builder.expected( "" );
    } );

  }

  // Implicit Iterators

  /*
   * Unadorned tags should interpolate content into the template.
   *
   * Ignored test: the API is defined to take a map as first level context.
   */
  // @Test
  public void implicitIteratorsBasicInterpolation() {

    MustacheTest.test( builder -> {
      // builder.data( "world" );
      builder.template( "Hello, {{.}}!" );
      builder.expected( "Hello, world!" );
    } );

  }

  /*
   * Basic interpolation should be HTML escaped.
   *
   * Ignored test: the API is defined to take a map as first level context.
   */
  // @Test
  public void implicitIteratorsHtmlEscaping() {

    MustacheTest.test( builder -> {
      // builder.data( "& \" < >" );
      builder.template( "These characters should be HTML escaped: {{.}}" );
      builder.expected( "These characters should be HTML escaped: &amp; &quot; &lt; &gt;" );
    } );

  }

  /*
   * Triple mustaches should interpolate without HTML escaping.
   *
   * Ignored test: the API is defined to take a map as first level context.
   */
  // @Test
  public void implicitIteratorsTripleMustache() {

    MustacheTest.test( builder -> {
      // builder.data( "& \" < >" );
      builder.template( "These characters should be HTML escaped: {{{.}}}" );
      builder.expected( "These characters should be HTML escaped: &amp; &quot; &lt; &gt;" );
    } );

  }

  /*
   * Ampersand should interpolate without HTML escaping.
   *
   * Ignored test: the API is defined to take a map as first level context.
   */
  // @Test
  public void implicitIteratorsAmpersand() {

    MustacheTest.test( builder -> {
      // builder.data( "& \" < >" );
      builder.template( "These characters should be HTML escaped: {{&.}}" );
      builder.expected( "These characters should be HTML escaped: &amp; &quot; &lt; &gt;" );
    } );

  }

  /*
   * Integers should interpolate seamlessly.
   *
   * Ignored test: the API is defined to take a map as first level context.
   */
  // @Test
  public void implicitIteratorsBasicIntegerInterpolation() {

    MustacheTest.test( builder -> {
      // builder.data( 85 );
      builder.template( "\"{{.}} miles an hour!\"" );
      builder.expected( "\"85 miles an hour!\"" );
    } );

  }

  // Whitespace Sensitivity

  /*
   * Interpolation should not alter surrounding whitespace.
   */
  @Test
  public void interpolationSurroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "| {{string}} |" );
      builder.expected( "| --- |" );
    } );

  }

  /*
   * Interpolation should not alter surrounding whitespace.
   */
  @Test
  public void tripleMustacheSurroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "| {{{string}}} |" );
      builder.expected( "| --- |" );
    } );

  }

  /*
   * Interpolation should not alter surrounding whitespace.
   */
  @Test
  public void ampersandSurroundingWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "| {{&string}} |" );
      builder.expected( "| --- |" );
    } );

  }

  /*
   * Standalone interpolation should not alter surrounding whitespace.
   */
  @Test
  public void interpolationStandalone() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "  {{string}}\n" );
      builder.expected( "  ---\n" );
    } );

  }

  /*
   * Standalone interpolation should not alter surrounding whitespace.
   */
  @Test
  public void tripleMustacheStandalone() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "  {{{string}}}\n" );
      builder.expected( "  ---\n" );
    } );

  }

  /*
   * Standalone interpolation should not alter surrounding whitespace.
   */
  @Test
  public void ampersandStandalone() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "  {{&string}}\n" );
      builder.expected( "  ---\n" );
    } );

  }

  // Whitespace Insensitivity

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void interpolationWithPadding() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "|{{ string }}|" );
      builder.expected( "|---|" );
    } );

  }

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void tripleMustacheWithPadding() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "|{{{ string }}}|" );
      builder.expected( "|---|" );
    } );

  }

  /*
   * Superfluous in-tag whitespace should be ignored.
   */
  @Test
  public void ampersandWithPadding() {

    MustacheTest.test( builder -> {
      builder.data( "string", "---" );
      builder.template( "|{{& string }}|" );
      builder.expected( "|---|" );
    } );

  }

}
