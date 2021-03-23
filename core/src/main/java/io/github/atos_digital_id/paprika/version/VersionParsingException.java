package io.github.atos_digital_id.paprika.version;

/**
 * Version parsing exception.
 **/
public class VersionParsingException extends Exception {

  private static final long serialVersionUID = 1L;

  public VersionParsingException() {}

  public VersionParsingException( String msg ) {
    super( msg );
  }

  public VersionParsingException( Throwable cause ) {
    super( cause );
  }

  public VersionParsingException( String msg, Throwable cause ) {
    super( msg, cause );
  }

  /**
   * Factory method.
   *
   * @param failed the problematic version.
   * @return an exception.
   **/
  public static VersionParsingException parsing( String failed ) {
    return new VersionParsingException( "Can not parse '" + failed + "' as a version." );
  }

}
