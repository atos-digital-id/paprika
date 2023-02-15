package io.github.atos_digital_id.paprika.utils.templating.engine.parser;

import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.COMMENT;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.DELIMITERS;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.END;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.INTERPOLATION;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.INTERPOLATION_RAW;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.INVERTED;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.PARTIAL;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.SECTION;
import static io.github.atos_digital_id.paprika.utils.templating.engine.parser.TagType.STRING;
import static java.util.regex.Pattern.DOTALL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.atos_digital_id.paprika.utils.templating.engine.segment.InterpolationSegment;
import io.github.atos_digital_id.paprika.utils.templating.engine.segment.InvertedSegment;
import io.github.atos_digital_id.paprika.utils.templating.engine.segment.PartialSegment;
import io.github.atos_digital_id.paprika.utils.templating.engine.segment.SectionSegment;
import io.github.atos_digital_id.paprika.utils.templating.engine.segment.Segment;
import io.github.atos_digital_id.paprika.utils.templating.engine.segment.StringSegment;

public class Parser {

  public static List<Segment> parse( String source ) {

    if( source == null || source.isEmpty() )
      return Collections.emptyList();

    List<Tag> tags = parseTags( source );
    List<TrimmedTag> trimmed = trimTags( tags );
    List<Segment> segments = convertTags( trimmed );

    return segments;

  }

  private static List<Tag> parseTags( String source ) {

    ParseStatus status = new ParseStatus( source );

    Position start = status.getCurrent();

    while( !status.eof() ) {
      if( status.isOpening() ) {

        Position end = status.getCurrent();
        String str = status.sub( start, end );
        Tag strTag = new Tag( STRING, str, start, end );
        status.add( strTag );

        Tag tag = parseTag( status );
        status.add( tag );

        start = status.getCurrent();

      } else {

        status.next();

      }
    }

    Position end = status.getCurrent();
    String str = status.sub( start, end );
    Tag strTag = new Tag( STRING, str, start, end );
    status.add( strTag );

    return status.getTags();

  }

  private static final Pattern TAG_UNESCAPED = Pattern.compile( "\\&\\s*(?<key>[^\\s]+)\\s*" );

  private static final Pattern TAG_START_SECTION = Pattern.compile( "\\#\\s*(?<key>[^\\s]+)\\s*" );

  private static final Pattern TAG_END_SECTION = Pattern.compile( "\\/\\s*(?<key>[^\\s]*)\\s*" );

  private static final Pattern TAG_INVERTED_SECTION =
      Pattern.compile( "\\^\\s*(?<key>[^\\s]*)\\s*" );

  private static final Pattern TAG_COMMENT = Pattern.compile( "\\!.*", DOTALL );

  private static final Pattern TAG_PARTIAL = Pattern.compile( "\\>\\s*(?<key>[^\\s]+)\\s*" );

  private static final Pattern TAG_DELIM =
      Pattern.compile( "\\=\\s*(?<start>[^=\\s]+)\\s+(?<end>[^=\\s]+)\\s*\\=" );

  private static final Pattern TAG_VARIABLE = Pattern.compile( "\\s*(?<key>[^\\s]+)\\s*" );

  private static Tag parseTag( ParseStatus status ) {

    Position start = status.getCurrent();
    status.next( status.getOpen().length() );

    if( status.peek() == '{' ) {

      status.next();
      Position keyStart = status.getCurrent();

      String escapedClose = "}" + status.getClose();

      while( !status.eof() && !status.match( escapedClose ) )
        status.next();

      if( status.eof() )
        ParseException.unclosedTag( start, escapedClose );

      String key = status.sub( keyStart, status.getCurrent() ).trim();

      status.next( escapedClose.length() );

      return new Tag( INTERPOLATION_RAW, key, start, status.getCurrent() );

    }

    Position keyStart = status.getCurrent();

    while( !status.eof() && !status.isClosing() )
      status.next();

    if( status.eof() )
      ParseException.unclosedTag( start, status.getClose() );

    String key = status.sub( keyStart, status.getCurrent() );
    status.next( status.getClose().length() );
    Position end = status.getCurrent();

    Matcher matcher;

    if( ( matcher = TAG_UNESCAPED.matcher( key ) ).matches() ) {

      return new Tag( INTERPOLATION_RAW, matcher.group( "key" ), start, end );

    } else if( ( matcher = TAG_START_SECTION.matcher( key ) ).matches() ) {

      return new Tag( SECTION, matcher.group( "key" ), start, end );

    } else if( ( matcher = TAG_END_SECTION.matcher( key ) ).matches() ) {

      return new Tag( END, matcher.group( "key" ), start, end );

    } else if( ( matcher = TAG_INVERTED_SECTION.matcher( key ) ).matches() ) {

      return new Tag( INVERTED, matcher.group( "key" ), start, end );

    } else if( ( matcher = TAG_COMMENT.matcher( key ) ).matches() ) {

      return new Tag( COMMENT, "", start, end );

    } else if( ( matcher = TAG_PARTIAL.matcher( key ) ).matches() ) {

      return new Tag( PARTIAL, matcher.group( "key" ), start, end );

    } else if( ( matcher = TAG_DELIM.matcher( key ) ).matches() ) {

      status.setOpen( matcher.group( "start" ) );
      status.setClose( matcher.group( "end" ) );

      return new Tag( DELIMITERS, "", start, end );

    } else if( ( matcher = TAG_VARIABLE.matcher( key ) ).matches() ) {

      return new Tag( INTERPOLATION, matcher.group( "key" ), start, end );

    } else {

      ParseException.unexpectedTag( start, key );
      return null; // unreachable

    }

  }

  private static final Pattern FIRST_END_SPACES = Pattern.compile( "(?<=\\A|\\R)\\h*\\z" );

  private static final Pattern END_SPACES = Pattern.compile( "(?<=\\R)\\h*\\z" );

  private static final Pattern LAST_START_EMPTY_LINE = Pattern.compile( "\\A\\h*(\\R|\\z)" );

  private static final Pattern START_EMPTY_LINE = Pattern.compile( "\\A\\h*\\R" );

  private static List<TrimmedTag> trimTags( List<Tag> tags ) {

    int s = tags.size();

    List<TrimmedTag> trimmed = new ArrayList<>( s );
    for( Tag tag : tags )
      trimmed.add( new TrimmedTag( tag, "", "" ) );

    for( int i = tags.size() - 1; i >= 0; i-- ) {

      TrimmedTag tag = trimmed.get( i );

      if( tag.getTag().isStandalone() ) {

        assert trimmed.get( i - 1 ).getType() == STRING;
        assert trimmed.get( i + 1 ).getType() == STRING;

        TrimmedTag prev = trimmed.get( i - 1 );
        String prevData = prev.getData();
        Pattern prevPattern = i == 1 ? FIRST_END_SPACES : END_SPACES;
        Matcher prevMatcher = prevPattern.matcher( prevData );

        TrimmedTag next = trimmed.get( i + 1 );
        String nextData = next.getData();
        Pattern nextPattern = i == s - 2 ? LAST_START_EMPTY_LINE : START_EMPTY_LINE;
        Matcher nextMatcher = nextPattern.matcher( nextData );

        if( prevMatcher.find() && nextMatcher.find() ) {

          String trimmedPrev = prevData.substring( 0, prevMatcher.start() );
          trimmed.set( i - 1, prev.withData( trimmedPrev ) );

          String trimmedNext = nextData.substring( nextMatcher.end() );
          trimmed.set( i + 1, next.withData( trimmedNext ) );

          trimmed.set( i, tag.withTrimmed( prevMatcher.group(), nextMatcher.group() ) );

        }

      }

    }

    return trimmed;

  }

  private static List<Segment> convertTags( List<TrimmedTag> tags ) {

    List<Segment> segments = new ArrayList<>();

    int end = convertTags( tags, 0, null, "", segments );
    if( end != tags.size() )
      ParseException.sectionNotOpened( tags.get( end ) );

    return segments;

  }

  private static int convertTags(
      List<TrimmedTag> tags,
      int current,
      TagType openedType,
      String openedData,
      List<Segment> out ) {

    while( current < tags.size() ) {

      TrimmedTag tag = tags.get( current );
      String data = tag.getData();

      switch( tag.getType() ) {

        case STRING:
          out.add( new StringSegment( data ) );
          break;

        case INTERPOLATION:

          if( data.isEmpty() )
            ParseException.emptyData( tag );

          out.add( new InterpolationSegment( data, true ) );
          break;

        case INTERPOLATION_RAW:

          if( data.isEmpty() )
            ParseException.emptyData( tag );

          out.add( new InterpolationSegment( data, false ) );
          break;

        case SECTION:

          if( data.isEmpty() || ( openedType == INVERTED && data.equals( openedData ) ) )
            return current;

          List<Segment> sectionSub = new ArrayList<>();
          current = convertTags( tags, current + 1, SECTION, data, sectionSub );
          if( current == tags.size() )
            ParseException.sectionUnexpectedEof( tag );

          out.add( new SectionSegment( data, sectionSub ) );

          TrimmedTag sectionClose = tags.get( current );
          if( sectionClose.getType() == INVERTED ) {

            List<Segment> elseSub = new ArrayList<>();
            current = convertTags( tags, current + 1, INVERTED, data, elseSub );
            if( current == tags.size() )
              ParseException.sectionUnexpectedEof( tag );

            out.add( new InvertedSegment( data, elseSub ) );

            sectionClose = tags.get( current );

          }
          if( sectionClose.getType() != END )
            ParseException.expectCloseTag( tag, sectionClose );

          String sectionCloseName = tags.get( current ).getData();
          if( !sectionCloseName.isEmpty() && !data.equals( sectionCloseName ) )
            ParseException.sectionMismatch( tag, sectionClose );

          break;

        case END:
          return current;

        case INVERTED:

          if( data.isEmpty() || ( openedType == SECTION && data.equals( openedData ) ) )
            return current;

          List<Segment> invertedSub = new ArrayList<>();
          current = convertTags( tags, current + 1, INVERTED, data, invertedSub );
          if( current == tags.size() )
            ParseException.sectionUnexpectedEof( tag );

          out.add( new InvertedSegment( data, invertedSub ) );

          TrimmedTag invertedClose = tags.get( current );
          if( invertedClose.getType() == INVERTED ) {

            List<Segment> elseSub = new ArrayList<>();
            current = convertTags( tags, current + 1, SECTION, data, elseSub );
            if( current == tags.size() )
              ParseException.sectionUnexpectedEof( tag );

            out.add( new SectionSegment( data, elseSub ) );

            invertedClose = tags.get( current );

          }
          if( invertedClose.getType() != END )
            ParseException.expectCloseTag( tag, invertedClose );

          String invertedCloseName = tags.get( current ).getData();
          if( !invertedCloseName.isEmpty() && !data.equals( invertedCloseName ) )
            ParseException.sectionMismatch( tag, invertedClose );

          break;

        case COMMENT:
          break;

        case PARTIAL:

          if( data.isEmpty() )
            ParseException.emptyData( tag );

          out.add( new PartialSegment( tag.getBefore(), data ) );

          break;

        case DELIMITERS:
          break;

      }

      current++;

    }

    return current;

  }

}
