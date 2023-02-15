package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.Lambda;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrapper around a date ({@link ZonedDateTime}).
 */
@Data
public class DateValue {

  /**
   * Create a wrapper around a date.
   *
   * @param date the date to wrap
   * @return the wrapped date, or {@code null} if the date is {@code null}.
   */
  public static DateValue wrap( ZonedDateTime date ) {
    return date == null ? null : new DateValue( date );
  }

  @NonNull
  @Getter( NONE )
  private final ZonedDateTime date;

  /**
   * Return a lambda to format the date. The format can be specified in the
   * included template:
   * <code>{{#myDate.format}}yyyy-MM-dd{{#myDate.format}}}</code>.
   *
   * @return a lambda to format the date.
   */
  public Lambda format() {

    return ( temp, out ) -> {
      DateTimeFormatter pattern = DateTimeFormatter.ofPattern( temp.execute() );
      out.append( date.format( pattern ) );
    };

  }

  /**
   * Get the date by the {@link DateTimeFormatter#BASIC_ISO_DATE} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String BASIC_ISO_DATE = date.format( DateTimeFormatter.BASIC_ISO_DATE );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_LOCAL_DATE} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_LOCAL_DATE = date.format( DateTimeFormatter.ISO_LOCAL_DATE );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_OFFSET_DATE} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_OFFSET_DATE = date.format( DateTimeFormatter.ISO_OFFSET_DATE );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_DATE} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_DATE = date.format( DateTimeFormatter.ISO_DATE );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_LOCAL_TIME} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_LOCAL_TIME = date.format( DateTimeFormatter.ISO_LOCAL_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_OFFSET_TIME} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_OFFSET_TIME = date.format( DateTimeFormatter.ISO_OFFSET_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_TIME} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_TIME = date.format( DateTimeFormatter.ISO_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_LOCAL_DATE_TIME}
   * formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_LOCAL_DATE_TIME = date.format( DateTimeFormatter.ISO_LOCAL_DATE_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}
   * formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_OFFSET_DATE_TIME = date.format( DateTimeFormatter.ISO_OFFSET_DATE_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_ZONED_DATE_TIME}
   * formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_ZONED_DATE_TIME = date.format( DateTimeFormatter.ISO_ZONED_DATE_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_DATE_TIME} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_DATE_TIME = date.format( DateTimeFormatter.ISO_DATE_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_ORDINAL_DATE} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_ORDINAL_DATE = date.format( DateTimeFormatter.ISO_ORDINAL_DATE );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_WEEK_DATE} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_WEEK_DATE = date.format( DateTimeFormatter.ISO_WEEK_DATE );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_INSTANT} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String ISO_INSTANT = date.format( DateTimeFormatter.ISO_INSTANT );

  /**
   * Get the date by the {@link DateTimeFormatter#RFC_1123_DATE_TIME} formatter.
   *
   * @return the formatted date.
   */
  @Getter( lazy = true )
  private final String RFC_1123_DATE_TIME = date.format( DateTimeFormatter.RFC_1123_DATE_TIME );

  /**
   * Get the date by the {@link DateTimeFormatter#ISO_DATE_TIME} formatter.
   *
   * @return the formatted date.
   * @see getISO_DATE_TIME
   */
  @Override
  public String toString() {
    return getISO_DATE_TIME();
  }

}
