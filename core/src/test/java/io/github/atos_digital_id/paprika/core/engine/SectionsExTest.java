package io.github.atos_digital_id.paprika.core.engine;

import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.emptyList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.simpleList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.emptyMap;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.simpleMap;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.singleMap;

import org.junit.jupiter.api.Test;

/**
 * Unofficial features: extended sections.
 *
 * Adapted from
 * https://github.com/groue/GRMustache/blob/v7.3.2/src/tests/Public/v7.0/Suites/groue/GRMustache/GRMustacheSuites/sections.json
 */
public class SectionsExTest {

  /*
   * Multiple sections per template should be permitted.
   */
  @Test
  public void multipleSections() {

    MustacheTest.test( builder -> {
      builder.data( "t", true );
      builder.data( "two", 2 );
      builder.template( "<{{#t}}1{{/t}}{{two}}{{#t}}3{{/t}}>" );
      builder.expected( "<123>" );
    } );

  }

  // Boolean interpretation

  /*
   * Missing key should trigger the section omission.
   */
  @Test
  public void missingKey() {

    MustacheTest.test( builder -> {
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<>" );
    } );

  }

  /*
   * Null should trigger the section omission.
   */
  @Test
  public void nullValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", null );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<>" );
    } );

  }

  /*
   * False should trigger the section omission.
   */
  @Test
  public void falseValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<>" );
    } );

  }

  /*
   * Empty string should trigger the section omission.
   */
  @Test
  public void emptyValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "" );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<>" );
    } );

  }

  /*
   * Empty list should trigger the section omission."
   */
  @Test
  public void emptyListValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", emptyList() );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<>" );
    } );

  }

  /*
   * Zero should trigger the section omission.
   */
  @Test
  public void zeroValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", 0 );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<>" );
    } );

  }

  /*
   * True should trigger the section rendering.
   */
  @Test
  public void trueValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * True should trigger the section rendering without altering the top of the
   * context stack.
   */
  @Test
  public void trueValueAlteringTopContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.data( "object", "---" );
      builder.template( "<{{#object}}{{#subject}}{{.}}{{/subject}}{{/object}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-zero number should trigger the section rendering.
   */
  @Test
  public void nonZeroValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", 1 );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-zero number should trigger the section rendering and enter the top of
   * the context stack.
   */
  @Test
  public void nonZeroValueAlteringTopContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", 1 );
      builder.data( "object", "---" );
      builder.template( "<{{#object}}{{#subject}}{{.}}{{/subject}}{{/object}}>" );
      // Conflict with
      // https://github.com/mustache/spec/blob/v1.3.0/specs/sections.yml#L92
      // builder.expected( "<--->" );
      builder.expected( "<1>" );
    } );

  }

  /*
   * Object should trigger the section rendering.
   */
  @Test
  public void objectValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", emptyMap() );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Object should trigger the section rendering and enter the top of the
   * context stack.
   */
  @Test
  public void objegctValueAlteringTopContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", singleMap( "object", "---" ) );
      builder.template( "<{{#subject}}{{object}}{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty string should trigger the section rendering.
   */
  @Test
  public void nonEmptyStringValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "---" );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty string should trigger the section rendering and enter the top of
   * the context stack.
   */
  @Test
  public void nonEmptyStringValueAlteringTopContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "---" );
      builder.template( "<{{#subject}}{{.}}{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty blank string should trigger the section rendering.
   */
  @Test
  public void nonEmptyBlankStringValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", " " );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty blank string should trigger the section rendering and enter the
   * top of the context stack.
   */
  @Test
  public void nonEmptyBlankStringValueAlteringTopContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", " " );
      builder.template( "<{{#subject}}{{.}}{{/subject}}>" );
      builder.expected( "< >" );
    } );

  }

  /*
   * List made of an empty list should trigger the section rendering.
   */
  @Test
  public void listOfEmptyListValue() {

    MustacheTest.test( builder -> {
      builder.data( "subject", simpleList( emptyList() ) );
      builder.template( "<{{#subject}}---{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * List made of lists should render each of them independently.
   */
  @Test
  public void listOfListsValue() {

    MustacheTest.test( builder -> {
      builder
          .data( "subject", simpleList( simpleList( "a", "b" ), emptyList(), simpleList( 0, 1 ) ) );
      builder.template( "<{{#subject}}({{#.}}{{.}}{{^}}-{{/}}){{/subject}}>" );
      builder.expected( "<(ab)(-)(01)>" );
    } );

  }

  // Context stack

  /*
   * Object should become the current context.
   */
  @Test
  public void setCurrentContext() {

    MustacheTest.test( builder -> {
      builder.data( "context", singleMap( "subject", "---" ) );
      builder.template( "<{{#context}}{{subject}}{{/context}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * A key miss should look in including context.
   */
  @Test
  public void lookIncludingContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "---" );
      builder.data( "context", emptyMap() );
      builder.template( "<{{#context}}{{subject}}{{/context}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Nested sections with same name should be isolated.
   */
  @Test
  public void nestedSectionsSameName() {

    MustacheTest.test( builder -> {
      builder.data(
          "context",
          simpleMap().add( "subject", "1" ).add( "context", singleMap( "subject", "a" ) ).build() );
      builder
          .template( "<{{#context}}{{subject}}{{#context}}{{subject}}{{/context}}{{/context}}>" );
      builder.expected( "<1a>" );
    } );

  }

  // Lists

  /*
   * Lists should be iterated.
   */
  @Test
  public void iteratingLists() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3 ) );
      builder.template( "<{{#list}}-{{/list}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * List items should become the current context.
   */
  @Test
  public void listItemAsCurrentContext() {

    MustacheTest.test( builder -> {
      builder.data(
          "list",
          simpleList( singleMap( "n", 1 ), singleMap( "n", 2 ), singleMap( "n", 3 ) ) );
      builder.template( "<{{#list}}{{n}}{{/list}}>" );
      builder.expected( "<123>" );
    } );

  }

  /*
   * A key miss should look in including context.
   */
  @Test
  public void keyMissInListContext() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "---" );
      builder.data(
          "list",
          simpleList( singleMap( "n", 1 ), singleMap( "n", 2 ), singleMap( "n", 3 ) ) );
      builder.template( "<{{#list}}{{subject}}{{n}}{{/list}}>" );
      builder.expected( "<---1---2---3>" );
    } );

  }

  /*
   * Nested sections with same name should be isolated.
   */
  @Test
  public void nestedListSameName() {

    MustacheTest.test( builder -> {
      builder.data(
          "context",
          simpleList(
              simpleMap().add( "subject", "1" )
                  .add(
                      "context",
                      simpleList(
                          singleMap( "subject", "a" ),
                          singleMap( "subject", "b" ),
                          singleMap( "subject", "c" ) ) )
                  .build() ) );
      builder
          .template( "<{{#context}}{{subject}}{{#context}}{{subject}}{{/context}}{{/context}}>" );
      builder.expected( "<1abc>" );
    } );

  }

  // 'else' sections

  /*
   * Missing key should trigger the section omission, and the rendering of the
   * 'else' section.
   */
  @Test
  public void missingValueElse() {

    MustacheTest.test( builder -> {
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Null should trigger the section omission, and the rendering of the 'else'
   * section.
   */
  @Test
  public void nullValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", null );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * False should trigger the section omission, and the rendering of the 'else'
   * section.
   */
  @Test
  public void falseValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Empty string should trigger the section omission, and the rendering of the
   * 'else' section.
   */
  @Test
  public void emptyStringValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "" );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Empty list should trigger the section omission, and the rendering of the
   * 'else' section.
   */
  @Test
  public void emptyListValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", emptyList() );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Zero should trigger the section omission, and the rendering of the 'else'
   * section.
   */
  @Test
  public void zeroValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", 0 );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * True should trigger the section rendering, and the omission of the 'else'
   * section.
   */
  @Test
  public void trueValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Object should trigger the section rendering, and the omission of the 'else'
   * section.
   */
  @Test
  public void objectValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", emptyMap() );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty string should trigger the section rendering, and the omission of
   * the 'else' section.
   */
  @Test
  public void nonEmtyStringValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", "---" );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty blank string should trigger the section rendering, and the
   * omission of the 'else' section.
   */
  @Test
  public void blankStringValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", " " );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * List made of an empty list should trigger the section rendering, and the
   * omission of the 'else' section.
   */
  @Test
  public void listOfEmptyListValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", simpleList( emptyList() ) );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/subject}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Non-empty lists should be iterated and the 'else' section omitted
   */
  @Test
  public void nonEmptyListValueElse() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3 ) );
      builder.template( "<{{#list}}-{{^list}}+{{/list}}>" );
      builder.expected( "<--->" );
    } );

  }

  // Empty 'else' and closing tags

  /*
   * Closing tag may be empty.
   */
  @Test
  public void emptyClosingTag() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}}---{{/}}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Closing tag may be blank.
   */
  @Test
  public void blankClosingTag() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}}---{{/ }}>" );
      builder.expected( "<--->" );
    } );

  }

  /*
   * Closing tag after 'else' tag may be empty.
   */
  @Test
  public void emptyClosingTagAfterElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Closing tag after 'else' tag may be blank.
   */
  @Test
  public void blankClosingTagAfterElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^subject}}+++{{/ }}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * 'Else' tag may be empty.
   */
  @Test
  public void emptyElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^}}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * 'Else' tag may be blank.
   */
  @Test
  public void blankElse() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^ }}+++{{/subject}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Both 'else' and closing tags may be empty.
   */
  @Test
  public void emptyElseAndClosing() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^}}+++{{/}}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Both 'else' and closing tags may be blank.
   */
  @Test
  public void blankElseAndClosing() {

    MustacheTest.test( builder -> {
      builder.data( "subject", false );
      builder.template( "<{{#subject}}---{{^ }}+++{{/ }}>" );
      builder.expected( "<+++>" );
    } );

  }

  /*
   * Empty 'else' and closing tags can be nested.
   */
  @Test
  public void nestedElseAndCosing() {

    MustacheTest.test( builder -> {
      builder.data( "subject1", false );
      builder.data( "subject2", false );
      builder.template(
          "<{{#subject1}}{{#foo}}---{{^ }}+++{{/ }}{{^ }}{{#subject2}}---{{^ }}+++{{/ }}{{/ }}>" );
      builder.expected( "<+++>" );
    } );

  }

  // Whitespace Insensitivity

  /*
   * Whitespace in tag should be ignored.
   */
  @Test
  public void ignoreWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{# \r\n\tsubject \r\n\t}}---{{/ \r\n\tsubject \r\n\t}}>" );
      builder.expected( "<--->" );
    } );

  }

  // Whitespace Sensitivity

  /*
   * Single left outer whitespace should be honored when section is rendered.
   */
  @Test
  public void leftOuterWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "< {{#subject}}---{{/subject}}>" );
      builder.expected( "< --->" );
    } );

  }

  /*
   * Single right outer whitespace should be honored when section is rendered.
   */
  @Test
  public void rightOuterWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}}---{{/subject}} >" );
      builder.expected( "<--- >" );
    } );

  }

  /*
   * Single left inner whitespace should be honored when section is rendered.
   */
  @Test
  public void leftInnerWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}} ---{{/subject}}>" );
      builder.expected( "< --->" );
    } );

  }

  /*
   * Single right inner whitespace should be honored when section is rendered.
   */
  @Test
  public void rightInnerWhitespace() {

    MustacheTest.test( builder -> {
      builder.data( "subject", true );
      builder.template( "<{{#subject}}--- {{/subject}}>" );
      builder.expected( "<--- >" );
    } );

  }

  /*
   * Single left outer whitespace should be honored.
   */
  @Test
  public void leftOuterWhitespaceWithList() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3 ) );
      builder.template( "< {{#list}}-{{/list}}>" );
      builder.expected( "< --->" );
    } );

  }

  /*
   * Single right outer whitespace should be honored.
   */
  @Test
  public void rightOuterWhitespaceWithList() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3 ) );
      builder.template( "<{{#list}}-{{/list}} >" );
      builder.expected( "<--- >" );
    } );

  }

  /*
   * Single left inner whitespace should be honored.
   */
  @Test
  public void leftInnerWhitespaceWithList() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3 ) );
      builder.template( "<{{#list}} -{{/list}}>" );
      builder.expected( "< - - ->" );
    } );

  }

  /*
   * Single right inner whitespace should be honored.
   */
  @Test
  public void rightInnerWhitespaceWithList() {

    MustacheTest.test( builder -> {
      builder.data( "list", simpleList( 1, 2, 3 ) );
      builder.template( "<{{#list}}- {{/list}}>" );
      builder.expected( "<- - - >" );
    } );

  }

  /*
   * Single left outer whitespace should be honored when section is omitted.
   */
  @Test
  public void leftOuterWhitespaceOmittedSection() {

    MustacheTest.test( builder -> {
      builder.template( "< {{#subject}}---{{/subject}}--->" );
      builder.expected( "< --->" );
    } );

  }

  /*
   * Single right outer whitespace should be honored when section is omitted.
   */
  @Test
  public void rightOuterWhitespaceOmittedSection() {

    MustacheTest.test( builder -> {
      builder.template( "<---{{#subject}}---{{/subject}} >" );
      builder.expected( "<--- >" );
    } );

  }

}
