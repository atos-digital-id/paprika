package io.github.atos_digital_id.paprika.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
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

  /**
   * Build a Pattern with a quote/unquoted characters support.
   **/
  private static class PatternBuilder {

    private final StringBuilder builder = new StringBuilder( "\\A" );

    private boolean quoted = false;

    public void addQuoted( char c ) {
      if( !quoted ) {
        builder.append( "\\Q" );
        quoted = true;
      }
      builder.append( c );
    }

    public void addUnquoted( String str ) {
      if( quoted ) {
        builder.append( "\\E" );
        quoted = false;
      }
      builder.append( str );
    }

    public Pattern build() {

      StringBuilder b = new StringBuilder( builder.toString() );
      if( quoted )
        b.append( "\\E" );
      b.append( "\\Z" );

      return Pattern.compile( b.toString() );

    }

  }

  /**
   * Result of test of a path pattern test.
   **/
  public enum PathFilterResult {

    /**
     * Don't match.
     **/
    NO_MATCH,

    /**
     * Match.
     **/
    MATCH,

    /**
     * Match and all sub tree also match.
     **/
    TREE_MATCH;

  }

  /**
   * Single tree filter (no support of ':' nor '!').
   **/
  private static class SimpleFilter {

    private final Pattern completePattern;

    private final List<Pattern> partialPatterns = new ArrayList<>();

    private boolean useDoubleWildcard = false;

    public SimpleFilter( String exp ) {

      PatternBuilder builder = new PatternBuilder();

      int len = exp.length();
      boolean partialConstruct = true;

      for( int i = 0; i < len; i++ ) {

        char c = exp.charAt( i );

        if( c == '\\' && i < len - 1 ) {
          builder.addQuoted( exp.charAt( ++i ) );
        } else if( partialConstruct && c == '/' ) {
          partialPatterns.add( builder.build() );
          builder.addQuoted( c );
        } else if( c == '?' ) {
          builder.addUnquoted( "." );
        } else if( c == '*' ) {
          if( i < len - 1 && exp.charAt( i + 1 ) == '*' ) {
            builder.addUnquoted( ".*" );
            ++i;
            partialConstruct = false;
            useDoubleWildcard = true;
          } else {
            builder.addUnquoted( "[^/]*" );
          }
        } else {
          builder.addQuoted( c );
        }

      }

      completePattern = builder.build();

    }

    private boolean test( Pattern pattern, String str ) {
      return pattern.matcher( str ).matches();
    }

    private boolean test( int idx, String str ) {
      return test( partialPatterns.get( idx ), str );
    }

    /**
     * Test if the given path match the pattern.
     *
     * @param path the path to test.
     * @return true if the string match the pattern.
     **/
    public boolean complete( String path ) {
      return test( completePattern, path );
    }

    /**
     * Test if the given string match partialy the pattern. In case of
     * {@code PathFilterResult.TREE_MATCH}, a complete test at the end is
     * necessary with the path of a regular file.
     *
     * @param path the path to test.
     * @return the match result.
     **/
    public PathFilterResult partial( int depth, String path ) {

      int maxi = partialPatterns.size() - 1;

      if( useDoubleWildcard ) {

        int i = Math.min( depth, maxi );

        return test( i, path )
            ? ( i == maxi ? PathFilterResult.TREE_MATCH : PathFilterResult.MATCH )
            : PathFilterResult.NO_MATCH;

      } else {

        if( depth > maxi )
          return PathFilterResult.NO_MATCH;

        return test( depth, path ) ? PathFilterResult.MATCH : PathFilterResult.NO_MATCH;

      }

    }

  }

  /**
   * Complete path filter (all features support).
   **/
  public static class PathFilter {

    private final List<SimpleFilter> excludes = new ArrayList<>();

    private final List<SimpleFilter> includes = new ArrayList<>();

    public PathFilter( String exp ) {

      int len = exp.length();

      List<SimpleFilter> list = includes;

      int start = 0;

      for( int i = 0; i < len; i++ ) {

        char c = exp.charAt( i );

        if( c == '\\' && i < len - 1 ) {
          i += 1;
        } else if( c == '!' && start == i ) {
          list = excludes;
          start = i + 1;
        } else if( c == ':' ) {
          list.add( new SimpleFilter( exp.substring( start, i ) ) );
          list = includes;
          start = i + 1;
        }

      }

      list.add( new SimpleFilter( exp.substring( start ) ) );

    }

    /**
     * Test if the given path match any pattern, and is not ignored.
     *
     * @param path the path to test.
     * @return true if the string match the pattern.
     **/
    public boolean complete( String path ) {

      for( SimpleFilter filter : excludes )
        if( filter.complete( path ) )
          return false;

      for( SimpleFilter filter : includes )
        if( filter.complete( path ) )
          return true;

      return false;

    }

    /**
     * Test if the given string match partialy a pattern. In case of
     * {@code PathFilterResult.TREE_MATCH}, a complete test at the end is
     * necessary with the path of a regular file.
     *
     * @param path the path to test.
     * @return the match result.
     **/
    public PathFilterResult partial( String path ) {

      int depth = 0;
      for( int i = 0; i < path.length(); i++ ) {
        char c = path.charAt( i );
        if( c == '/' )
          depth += 1;
      }

      for( SimpleFilter filter : includes ) {
        PathFilterResult res = filter.partial( depth, path );
        if( res == PathFilterResult.MATCH || res == PathFilterResult.TREE_MATCH )
          return res;
      }

      return PathFilterResult.NO_MATCH;

    }

  }

  private static final PathFilter NULL_PATH_FILTER = new PathFilter( "" ) {

    @Override
    public boolean complete( String path ) {
      return false;
    }

    @Override
    public PathFilterResult partial( String path ) {
      return PathFilterResult.NO_MATCH;
    }

  };

  /**
   * Parse a wildcard pattern and return a predicate based on it. The predicate
   * returns true if the tested value is matched by the wildcard expression.
   *
   * @param exp the wildcard expression.
   * @return a predicate based on the wildcard expression.
   **/
  public static PathFilter pathFilter( String exp ) {
    return exp == null ? NULL_PATH_FILTER : new PathFilter( exp );
  }

}
