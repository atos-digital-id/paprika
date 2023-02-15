package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;

public class SimpleCustomMapBuilder {

  private static final CustomMap EMPTY =
      new AbstractCustomMap( Collections.emptyMap(), UnaryOperator.identity(), ":", " " );

  private final Map<String, Object> map = new HashMap<>();

  public SimpleCustomMapBuilder add( String key, Object value ) {
    map.put( key, value );
    return this;
  }

  public CustomMap build() {
    return new AbstractCustomMap( map, UnaryOperator.identity(), ":", " " );
  }

  public static CustomMap emptyMap() {
    return EMPTY;
  }

  public static SimpleCustomMapBuilder simpleMap() {
    return new SimpleCustomMapBuilder();
  }

  public static CustomMap singleMap( String key, Object value ) {
    return simpleMap().add( key, value ).build();
  }

}
