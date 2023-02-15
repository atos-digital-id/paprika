package io.github.atos_digital_id.paprika.utils.templating.engine.parser;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum TagType {

  STRING( false ),

  INTERPOLATION( false ),

  INTERPOLATION_RAW( false ),

  SECTION( true ),

  END( true ),

  INVERTED( true ),

  COMMENT( true ),

  PARTIAL( true ),

  DELIMITERS( true );

  @Getter
  private final boolean standalone;

}
