package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.Lambda;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public class DateValue {

  public static DateValue wrap( ZonedDateTime date ) {
    return date == null ? null : new DateValue( date );
  }

  @NonNull
  @Getter( NONE )
  private final ZonedDateTime date;

  public Lambda format() {

    return ( temp, out ) -> {
      DateTimeFormatter pattern = DateTimeFormatter.ofPattern( temp.execute() );
      out.append( date.format( pattern ) );
    };

  }

  @Getter( lazy = true )
  private final String BASIC_ISO_DATE = date.format( DateTimeFormatter.BASIC_ISO_DATE );

  @Getter( lazy = true )
  private final String ISO_LOCAL_DATE = date.format( DateTimeFormatter.ISO_LOCAL_DATE );

  @Getter( lazy = true )
  private final String ISO_OFFSET_DATE = date.format( DateTimeFormatter.ISO_OFFSET_DATE );

  @Getter( lazy = true )
  private final String ISO_DATE = date.format( DateTimeFormatter.ISO_DATE );

  @Getter( lazy = true )
  private final String ISO_LOCAL_TIME = date.format( DateTimeFormatter.ISO_LOCAL_TIME );

  @Getter( lazy = true )
  private final String ISO_OFFSET_TIME = date.format( DateTimeFormatter.ISO_OFFSET_TIME );

  @Getter( lazy = true )
  private final String ISO_TIME = date.format( DateTimeFormatter.ISO_TIME );

  @Getter( lazy = true )
  private final String ISO_LOCAL_DATE_TIME = date.format( DateTimeFormatter.ISO_LOCAL_DATE_TIME );

  @Getter( lazy = true )
  private final String ISO_OFFSET_DATE_TIME = date.format( DateTimeFormatter.ISO_OFFSET_DATE_TIME );

  @Getter( lazy = true )
  private final String ISO_ZONED_DATE_TIME = date.format( DateTimeFormatter.ISO_ZONED_DATE_TIME );

  @Getter( lazy = true )
  private final String ISO_DATE_TIME = date.format( DateTimeFormatter.ISO_DATE_TIME );

  @Getter( lazy = true )
  private final String ISO_ORDINAL_DATE = date.format( DateTimeFormatter.ISO_ORDINAL_DATE );

  @Getter( lazy = true )
  private final String ISO_WEEK_DATE = date.format( DateTimeFormatter.ISO_WEEK_DATE );

  @Getter( lazy = true )
  private final String ISO_INSTANT = date.format( DateTimeFormatter.ISO_INSTANT );

  @Getter( lazy = true )
  private final String RFC_1123_DATE_TIME = date.format( DateTimeFormatter.RFC_1123_DATE_TIME );

  @Override
  public String toString() {
    return getISO_DATE_TIME();
  }

}
