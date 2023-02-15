package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleCustomListBuilder {

  private final static CustomList EMPTY =
      new AbstractCustomList( Collections.emptyList(), ", " ) {};

  private final List<Object> list = new ArrayList<>();

  public SimpleCustomListBuilder add( Object elt ) {
    list.add( elt );
    return this;
  }

  public CustomList build() {
    return new AbstractCustomList( list, ", " ) {};
  }

  public static CustomList emptyList() {
    return EMPTY;
  }

  public static SimpleCustomListBuilder simpleList() {
    return new SimpleCustomListBuilder();
  }

  public static CustomList simpleList( Object ... elts ) {
    SimpleCustomListBuilder builder = new SimpleCustomListBuilder();
    for( Object elt : elts )
      builder.add( elt );
    return builder.build();
  }

}
