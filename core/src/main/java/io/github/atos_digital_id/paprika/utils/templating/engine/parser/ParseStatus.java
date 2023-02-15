package io.github.atos_digital_id.paprika.utils.templating.engine.parser;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.NonNull;

@Data
public class ParseStatus {

  @NonNull
  private final String source;

  public String sub( Position s, Position e ) {
    return source.substring( s.getPos(), e.getPos() );
  }

  private Position current = new Position( 0, 0, 0 );

  public boolean bof( Position p ) {
    return p.getPos() == 0;
  }

  public boolean bof() {
    return bof( current );
  }

  public boolean eof( Position p ) {
    return p.getPos() >= source.length();
  }

  public boolean eof() {
    return eof( current );
  }

  public int peek( Position p, int offset ) {
    int pos = p.getPos() + offset;
    return pos >= source.length() ? -1 : source.codePointAt( pos );
  }

  public int peek( int offset ) {
    return peek( current, offset );
  }

  public int peek() {
    return peek( current, 0 );
  }

  public Position next( int i ) {

    if( eof() )
      return current;

    int len = source.length();

    int pos = current.getPos();
    int line = current.getLine();
    int col = current.getCol();

    for( int j = 0; j < i; j++ ) {

      pos++;
      if( pos >= len ) {
        current = new Position( line, col, pos );
        return current;
      }

      int c = peek( current, j );
      boolean nl = ( c == 0x0D && peek( current, j + 1 ) != 0x0A )
          || c == 0x0A
          || c == 0x0B
          || c == 0x0C
          || c == 0x85
          || c == 0x2028
          || c == 0x2029;

      if( nl ) {
        line++;
        col = 0;
      } else {
        col++;
      }

    }

    current = new Position( line, col, pos );

    return current;

  }

  public Position next() {
    return next( 1 );
  }

  public boolean match( String str ) {
    for( int i = 0; i < str.length(); i++ )
      if( str.codePointAt( i ) != peek( i ) )
        return false;
    return true;
  }

  private String open = "{{";

  public boolean isOpening() {
    return match( open );
  }

  private String close = "}}";

  public boolean isClosing() {
    return match( close );
  }

  private final List<Tag> tags = new ArrayList<>();

  public void add( Tag tag ) {
    tags.add( tag );
  }

}
