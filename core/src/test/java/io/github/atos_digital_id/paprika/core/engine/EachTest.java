package io.github.atos_digital_id.paprika.core.engine;

import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.emptyList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomListBuilder.simpleList;
import static io.github.atos_digital_id.paprika.utils.templating.engine.api.SimpleCustomMapBuilder.singleMap;

import org.junit.jupiter.api.Test;

/**
 * Unoffical feature.
 *
 * During the iteration of a list, additional keys can be accessed:
 * <ul>
 * <li>{@code @first} is true for the first item only.
 * <li>{@code @last} is true for the last item only.
 * <li>{@code @index} contains the 0-based index of the item (0, 1, 2,
 * etc.)</li>
 * <li>{@code @indexPlusOne} contains the 1-based index of the item (1, 2, 3,
 * etc.)</li>
 * <li>{@code @indexIsEven} is true if the 0-based index is even.</li>
 * </ul>
 */
public class EachTest {

  /*
   * `@first`, `@last` keys available for rendering.
   */
  @Test
  public void firstAndLast() {

    MustacheTest.test( builder -> {
      builder.data( "array", simpleList( "a", "b", "c", "d" ) );
      builder.template(
          "{{#array}}{{^@first}}{{#@last}} and {{/@last}}{{^@last}}, {{/@last}}{{/@first}}{{.}}{{/array}}" );
      builder.expected( "a, b, c and d" );
    } );

  }

  /*
   * `@index` key available for rendering.
   */
  @Test
  public void index() {

    MustacheTest.test( builder -> {
      builder.data( "array", simpleList( "a", "b", "c", "d" ) );
      builder.template( "{{#array}}{{@index}}{{/array}}" );
      builder.expected( "0123" );
    } );

  }

  /*
   * `@indexPlusOne` key available for rendering.
   */
  @Test
  public void indexPlusOne() {

    MustacheTest.test( builder -> {
      builder.data( "array", simpleList( "a", "b", "c", "d" ) );
      builder.template( "{{#array}}{{@indexPlusOne}}{{/array}}" );
      builder.expected( "1234" );
    } );

  }

  /*
   * `@indexIsEven` key available for rendering.
   */
  @Test
  public void indexIsEven() {

    MustacheTest.test( builder -> {
      builder.data( "array", simpleList( "a", "b", "c", "d" ) );
      builder.template(
          "{{#array}}{{#@indexIsEven}}e{{/@indexIsEven}}{{^@indexIsEven}}o{{/@indexIsEven}}{{/array}}" );
      builder.expected( "eoeo" );
    } );

  }

  /*
   * Don't alter context access.
   */
  @Test
  public void contextAccess() {

    MustacheTest.test( builder -> {
      builder.data( "array", simpleList( singleMap( "name", "foo" ), singleMap( "name", "bar" ) ) );
      builder.template( "{{#array}}<{{@index}}:{{name}}>{{/array}}" );
      builder.expected( "<0:foo><1:bar>" );
    } );

  }

  /*
   * Should render independently all lists of an array.
   */
  @Test
  public void contextRenderIndependently() {

    MustacheTest.test( builder -> {
      builder
          .data( "array", simpleList( simpleList( "a", "b" ), emptyList(), simpleList( 0, 1 ) ) );
      builder.template( "<{{#array}}{{@index}}:({{#.}}{{.}}{{/.}}{{^.}}-{{/.}}){{/array}}>" );
      builder.expected( "<0:(ab)1:(-)2:(01)>" );
    } );

  }

}
