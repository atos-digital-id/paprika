package io.github.atos_digital_id.paprika.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Patterns and wildcards utilities.
 **/
public class Patterns {

  private Patterns() {}

  /**
   * Splits a string around the given char. Works in a way similar of
   * {@link String#split(String)}.
   *
   * @param value the string to split.
   * @param sep the separator to use.
   * @return the array of string.
   **/
  public static List<String> split( String value, char sep ) {

    if( value == null || value.isEmpty() )
      return Collections.emptyList();

    ArrayList<String> list = new ArrayList<>();

    int off = 0;
    int next = 0;
    while( ( next = value.indexOf( sep, off ) ) != -1 ) {
      list.add( value.substring( off, next ) );
      off = next + 1;
    }

    if( off == 0 )
      list.add( value );
    else
      list.add( value.substring( off ) );

    return list;

  }

  private static class PatternBuilder {

    private final StringBuilder builder = new StringBuilder( "\\A" );

    private boolean start = true;

    private boolean quoted = false;

    public void addQuoted( char c ) {
      if( !quoted ) {
        builder.append( "\\Q" );
        quoted = true;
      }
      builder.append( c );
      start = false;
    }

    public void addUnquoted( String str ) {
      if( quoted ) {
        builder.append( "\\E" );
        quoted = false;
      }
      builder.append( str );
      start = false;
    }

    public boolean start() {
      return this.start;
    }

    public Pattern build() {
      this.addUnquoted( "\\Z" );
      return Pattern.compile( builder.toString() );
    }

  }

  /**
   * Parse a wildcard pattern and return a predicate based on it. The predicate
   * returns true if the tested value is matched by the wildcard expression.
   *
   * @param exp the wildcard expression.
   * @return a predicate based on the wildcard expression.
   **/
  public static Predicate<String> matcher( String exp ) {

    List<Pattern> excludes = new LinkedList<>();
    List<Pattern> includes = new LinkedList<>();

    int len = exp.length();

    PatternBuilder builder = new PatternBuilder();
    List<Pattern> list = includes;

    for( int i = 0; i < len; i++ ) {

      char c = exp.charAt( i );

      if( c == '\\' && i < len - 1 ) {
        builder.addQuoted( exp.charAt( ++i ) );
      } else if( c == '!' && builder.start() ) {
        list = excludes;
      } else if( c == '?' ) {
        builder.addUnquoted( "." );
      } else if( c == '*' ) {
        if( i < len - 1 && exp.charAt( i + 1 ) == '*' ) {
          builder.addUnquoted( ".*" );
          ++i;
        } else {
          builder.addUnquoted( "[^/]*" );
        }
      } else if( c == ':' ) {
        list.add( builder.build() );
        builder = new PatternBuilder();
        list = includes;
      } else {
        builder.addQuoted( c );
      }

    }

    list.add( builder.build() );

    return str -> {

      for( Pattern pattern : excludes )
        if( pattern.matcher( str ).matches() )
          return false;

      for( Pattern pattern : includes )
        if( pattern.matcher( str ).matches() )
          return true;

      return false;

    };

  }

}
