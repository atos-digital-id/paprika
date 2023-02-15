package io.github.atos_digital_id.paprika.utils.templating.engine;

import java.util.function.UnaryOperator;

public enum Escaper {

  NONE( UnaryOperator.identity() ),

  HTML( string -> {

    int len = string.length();

    StringBuilder builder = new StringBuilder( len );

    for( int i = 0; i < len; i++ ) {

      char c = string.charAt( i );

      if( c == '&' )
        builder.append( "&amp;" );
      else if( c == '\'' )
        builder.append( "&#39;" );
      else if( c == '"' )
        builder.append( "&quot;" );
      else if( c == '<' )
        builder.append( "&lt;" );
      else if( c == '>' )
        builder.append( "&gt;" );
      else if( c == '`' )
        builder.append( "&#x60;" );
      else if( c == '=' )
        builder.append( "&#x3D;" );
      else
        builder.append( c );

    }

    return builder.toString();

  } ),

  JSON( string -> {

    int len = string.length();

    StringBuilder builder = new StringBuilder( len );

    for( int i = 0; i < len; i++ ) {

      char c = string.charAt( i );

      if( c == '\"' )
        builder.append( "\\\"" );
      else if( c == '\\' )
        builder.append( "\\\\" );
      else if( c < 0x0020 ) {
        if( c == '\b' )
          builder.append( "\\b" );
        else if( c == '\f' )
          builder.append( "\\f" );
        else if( c == '\n' )
          builder.append( "\\n" );
        else if( c == '\r' )
          builder.append( "\\r" );
        else if( c == '\t' )
          builder.append( "\\t" );
        else
          builder.append( String.format( "\\u%04x", (int) c ) );
      } else
        builder.append( c );

    }

    return builder.toString();

  } );

  private final UnaryOperator<String> transform;

  Escaper( UnaryOperator<String> transform ) {
    this.transform = transform;
  }

  public String escape( String string ) {
    return transform.apply( string );
  }

}
