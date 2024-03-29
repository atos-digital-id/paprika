package io.github.atos_digital_id.paprika.utils.templating.value;

import static java.util.regex.Pattern.CASE_INSENSITIVE;
import static java.util.regex.Pattern.MULTILINE;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.AbstractCustomList;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.AbstractCustomMap;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.AbstractCustomStringList;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrapper around a commit message, parsing it as a
 * <a href="https://www.conventionalcommits.org/en/v1.0.0/">Conventional
 * Commit</a> to expose the different parts. Footer's values are splitted with
 * one value by line, then merged if several footers share the same key.
 **/
@Data
@Builder
public class CommitMessageValue {

  private static final Pattern FIRST_LINE = Pattern.compile( "\\A(.*)$", MULTILINE );

  private static final Pattern FOOTER_START = Pattern
      .compile( "(?<=\\R)\\h*\\R(?=([A-Z0-9_-]+|BREAKING CHANGE)\\s*(:|#))", CASE_INSENSITIVE );

  private static final Pattern FOOTER_KEY = Pattern
      .compile( "^(?<key>[A-Z0-9_-]+|BREAKING CHANGE)\\s*(:|#)", CASE_INSENSITIVE | MULTILINE );

  private static final Pattern CONVENTIONAL_FIRST_PATTERN = Pattern.compile(
      "(?<type>[^(!:\\s]+)\\s*(\\((?<scope>[^)]+)\\))?\\s*(?<breaking>!?)\\s*:\\s*(?<desc>.*)" );

  private static final String BREAKING_CHANGE = "BREAKING CHANGE";

  private static final String BREAKING_CHANGE_TIRET = "BREAKING-CHANGE";

  private static String canonicFooterKey( @NonNull Object key ) {
    String k = key.toString().trim().toUpperCase();
    return k.equals( BREAKING_CHANGE ) ? BREAKING_CHANGE_TIRET : k;
  }

  /**
   * Create a new {@code CommitMessageValue} wrapping the given commit message.
   *
   * @param message the commit message.
   * @return a new {@code CommitMessageValue}.
   **/
  public static CommitMessageValue wrap( @NonNull String message ) {

    CommitMessageValue.CommitMessageValueBuilder builder = CommitMessageValue.builder();

    builder.full( message );

    Matcher firstLineMatcher = FIRST_LINE.matcher( message );
    if( !firstLineMatcher.find() )
      throw new IllegalStateException( "No first line find in '" + message + "'." );
    String firstLine = firstLineMatcher.group( 1 ).trim();
    builder.firstLine( firstLine );

    Matcher footerMatcher = FOOTER_START.matcher( message );
    boolean findFooters = footerMatcher.find();

    int bodyEnd = findFooters ? footerMatcher.start() : message.length();
    String body = message.substring( firstLineMatcher.end(), bodyEnd ).trim();
    builder.body( body );

    Map<String, List<String>> footers = new HashMap<>();
    if( findFooters ) {

      footerMatcher.usePattern( FOOTER_KEY );

      String key = null;
      int valuesStart = 0;
      while( footerMatcher.find() ) {

        if( key != null ) {
          List<String> values = footers.computeIfAbsent( key, k -> new ArrayList<>() );
          for( String v : message.substring( valuesStart, footerMatcher.start() ).split( "\\R" ) ) {
            String t = v.trim();
            if( !t.isEmpty() )
              values.add( t );
          }
        }

        key = canonicFooterKey( footerMatcher.group( "key" ) );
        valuesStart = footerMatcher.end();

      }
      List<String> values = footers.computeIfAbsent( key, k -> new ArrayList<>() );
      for( String v : message.substring( valuesStart ).split( "\\R" ) ) {
        String t = v.trim();
        if( !t.isEmpty() )
          values.add( t );
      }

    }

    Matcher firstMatcher = CONVENTIONAL_FIRST_PATTERN.matcher( firstLine );
    if( firstMatcher.matches() ) {

      builder.isConventional( true );

      builder.type( firstMatcher.group( "type" ).toLowerCase().trim() );

      builder.scope( new ScopeValue( firstMatcher.group( "scope" ) ) );

      String description = firstMatcher.group( "desc" ).trim();
      builder.description( description );

      if( !firstMatcher.group( "breaking" ).isBlank()
          && !footers.keySet().stream().anyMatch( BREAKING_CHANGE_TIRET::equals ) )
        footers.put( BREAKING_CHANGE_TIRET, Collections.singletonList( description ) );

    }

    List<FooterValue> footersValues = new ArrayList<>();
    for( String key : footers.keySet() )
      footersValues.add( new FooterValue( key, footers.get( key ) ) );
    Collections.sort( footersValues );
    builder.footers( new FooterListValue( footersValues ) );

    return builder.build();

  }

  /**
   * The complete commit message.
   *
   * @return the complete message.
   **/
  @NonNull
  private final String full;

  /**
   * The first line a the commit message. The end of line characters are not
   * included.
   *
   * @return the first line.
   */
  @NonNull
  private final String firstLine;

  /**
   * Check if the message is conventional (following the Conventional Commit
   * convention).
   *
   * @return {@code true} if the message is conventional.
   */
  @Builder.Default
  private final boolean isConventional = false;

  /**
   * Extract the type of the message, if the message is conventional.
   */
  @NonNull
  @Builder.Default
  private final String type = "";

  /**
   * Wrapper around scopes.
   */
  public static class ScopeValue extends AbstractCustomStringList {

    private static String canonic( String scope ) {
      return scope.toLowerCase().trim();
    }

    private static List<String> canonicSplit( String scopes ) {
      if( scopes == null || scopes.isBlank() )
        return Collections.emptyList();
      return canonic( Arrays.asList( scopes.split( "," ) ) );
    }

    private static List<String> canonic( List<String> scopes ) {
      if( scopes == null )
        return Collections.emptyList();
      List<String> list = new ArrayList<>( scopes.size() );
      for( String s : scopes ) {
        String c = canonic( s );
        if( !c.isEmpty() )
          list.add( c );
      }
      return list;
    }

    public ScopeValue( List<String> scopes ) {
      super( canonic( scopes ), ",", ScopeValue::canonic );
    }

    public ScopeValue( String scopes ) {
      this( canonicSplit( scopes ) );
    }

  }

  /**
   * Extract scope element, if the message is conventional. Several scopes can
   * be defined, separated be commas.
   *
   * @return a representation of the scopes, if any.
   */
  @NonNull
  @Builder.Default
  private final ScopeValue scope = new ScopeValue( Collections.emptyList() );

  /**
   * Check if the message alerts about a breaking change, if the message is
   * conventional. A message is alerting about a breaking change if ther is a
   * bang after the type and the scopes, of if a footer is defined the key
   * {@code BREAKING CHANGE} or {@code BREAKING-CHANGE} (case insensitive).
   *
   * @return {@code true} if the message is alerting about a breaking change.
   */
  @Getter( lazy = true )
  private final boolean breaking = getFooter().isBreaking();

  /**
   * Returns the description part, if the message is conventional.
   *
   * @return the description.
   */
  @NonNull
  @Builder.Default
  private final String description = "";

  /**
   * Returns the body part, if the message is conventional.
   *
   * @return the body.
   */
  @NonNull
  @Builder.Default
  private final String body = "";

  /**
   * Wrapper around a footer (key/values).
   **/
  @Data
  public static class FooterValue extends AbstractCustomList<String> implements
      Comparable<FooterValue> {

    public FooterValue( String key, List<String> values ) {
      super( values, System.lineSeparator() );
      this.key = key;
    }

    /**
     * Key of the footer (upper case).
     *
     * @return the key of the footer.
     **/
    private final String key;

    /**
     * Check if the footer describes a breaking change.
     *
     * @return {@code true} if the footer describes a breaking change.
     **/
    @Getter( lazy = true )
    private final boolean breaking = key.equals( BREAKING_CHANGE_TIRET );

    @Override
    public int compareTo( @NonNull FooterValue other ) {

      String k = this.key;
      String o = other.key;

      if( k.equals( o ) )
        return 0;
      else if( k.equals( BREAKING_CHANGE_TIRET ) )
        return -1;
      else if( o.equals( BREAKING_CHANGE_TIRET ) )
        return 1;
      else
        return k.compareTo( o );

    }

    @Override
    public boolean equals( Object obj ) {
      if( this == obj )
        return true;
      if( obj == null )
        return false;
      if( obj instanceof FooterValue ) {
        FooterValue casted = (FooterValue) obj;
        return key.equals( casted.key ) && list.equals( casted.list );
      }
      return false;
    }

    @Override
    public int hashCode() {
      return key.hashCode();
    }

  }

  /**
   * Iterable list of footers.
   */
  public static class FooterListValue extends AbstractCustomList<FooterValue> {

    public FooterListValue( List<FooterValue> footers ) {
      super( footers, System.lineSeparator() );
    }

  }

  /**
   * Footers of the message, if the message is conventional, wrapped in an
   * iterable list.
   *
   * @return iterable list of the footers.
   */
  @NonNull
  @Builder.Default
  private final FooterListValue footers = new FooterListValue( Collections.emptyList() );

  /**
   * Map key/value for footer access.
   */
  public static class FooterMapValue extends AbstractCustomMap<FooterValue> {

    public FooterMapValue( Map<String, FooterValue> footers ) {
      super( footers, CommitMessageValue::canonicFooterKey, ": ", System.lineSeparator() );
    }

    /**
     * Check if a footer alerts about a breaking change.
     *
     * @return {@code true} if a footer is alerting about a breaking change.
     */
    @Getter( lazy = true )
    private final boolean breaking = map.values().stream().anyMatch( FooterValue::isBreaking );

  }

  /**
   * Footers of the message, if the message is conventional, wrapped in a map
   * indexed by the key of the footer.
   */
  @Getter( lazy = true )
  private final FooterMapValue footer = computeFooterMap();

  private FooterMapValue computeFooterMap() {

    Map<String, FooterValue> map = new HashMap<>();
    for( int i = 0; i < footers.size(); i++ ) {
      FooterValue footer = footers.get( i );
      map.put( footer.getKey(), footer );
    }

    return new FooterMapValue( map );

  }

  /**
   * Return the first line of the comment. For the complete message, see
   * {@link getFull}.
   *
   * @return the first line of the comment.
   * @see getFirstLine
   */
  @Override
  public String toString() {
    return firstLine;
  }

  public static class CommitMessageValueBuilder {

    public CommitMessageValueBuilder scopes( String ... scope ) {
      return this.scope( new ScopeValue( Arrays.asList( scope ) ) );
    }

    public CommitMessageValueBuilder multilineBody( String ... body ) {
      return this.body( String.join( "\n", body ) );
    }

    public class FooterBuilder {

      private List<FooterValue> footers = new ArrayList<>();

      public FooterBuilder add( String key, String ... values ) {
        footers.add( new FooterValue( key, Arrays.asList( values ) ) );
        return this;
      }

      public CommitMessageValueBuilder build() {
        return CommitMessageValueBuilder.this.footers( new FooterListValue( footers ) );
      }

    }

    public FooterBuilder footerBuilder() {
      return new FooterBuilder();
    }

  }

}
