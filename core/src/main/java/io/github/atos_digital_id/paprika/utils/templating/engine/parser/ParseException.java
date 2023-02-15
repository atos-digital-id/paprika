package io.github.atos_digital_id.paprika.utils.templating.engine.parser;

public class ParseException extends RuntimeException {

  public ParseException( String message ) {
    super( message );
  }

  public static void unclosedTag( Position pos, String expectedClose ) {
    throw new ParseException(
        "The tag opened at line "
            + ( pos.getLine() + 1 )
            + " column "
            + ( pos.getCol() + 1 )
            + " is never close: sequence '"
            + expectedClose
            + "' not found." );
  }

  public static void emptyData( TrimmedTag tag ) {
    Position pos = tag.getStart();
    throw new ParseException(
        "The tag at line "
            + ( pos.getLine() + 1 )
            + " column "
            + ( pos.getCol() + 1 )
            + " is empty." );
  }

  // section

  public static void sectionUnexpectedEof( TrimmedTag tag ) {
    Position pos = tag.getStart();
    throw new ParseException(
        "The section opened at line "
            + ( pos.getLine() + 1 )
            + " column "
            + ( pos.getCol() + 1 )
            + " on '"
            + tag.getData()
            + "' is never closed." );
  }

  public static void expectCloseTag( TrimmedTag open, TrimmedTag close ) {
    Position opos = open.getStart();
    Position cpos = close.getStart();
    throw new ParseException(
        "The tag at line "
            + ( cpos.getLine() + 1 )
            + " column "
            + ( cpos.getCol() + 1 )
            + " should close the tag at line "
            + ( opos.getLine() + 1 )
            + " column "
            + ( opos.getCol() + 1 )
            + "." );
  }

  public static void sectionNotOpened( TrimmedTag tag ) {

    Position pos = tag.getStart();
    String data = tag.getData();

    if( data.isBlank() ) {
      throw new ParseException(
          "The anonymous tag at line "
              + ( pos.getLine() + 1 )
              + " column "
              + ( pos.getCol() + 1 )
              + " close a not existing section." );
    } else {
      throw new ParseException(
          "The tag at line "
              + ( pos.getLine() + 1 )
              + " column "
              + ( pos.getCol() + 1 )
              + " close a not existing '"
              + data
              + "' section." );
    }

  }

  public static void sectionMismatch( TrimmedTag open, TrimmedTag close ) {

    Position pos = close.getStart();

    throw new ParseException(
        "The tag at line "
            + ( pos.getLine() + 1 )
            + " column "
            + ( pos.getCol() + 1 )
            + " close an mismatched section '"
            + close.getData()
            + "' (expected: '"
            + open.getData()
            + "')." );

  }

  public static void unexpectedTag( Position pos, String tag ) {

    throw new ParseException(
        "The tag at line "
            + ( pos.getLine() + 1 )
            + " column "
            + ( pos.getCol() + 1 )
            + " can not be parsed: '"
            + tag
            + "'." );

  }

}
