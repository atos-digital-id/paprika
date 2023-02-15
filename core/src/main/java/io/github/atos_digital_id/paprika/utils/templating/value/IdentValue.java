package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import java.time.ZonedDateTime;

import org.eclipse.jgit.lib.PersonIdent;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public class IdentValue {

  public static IdentValue wrap( PersonIdent ident ) {
    return ident == null ? null : new IdentValue( ident );
  }

  @NonNull
  @Getter( NONE )
  private final PersonIdent ident;

  @Getter( lazy = true )
  private final String name = ident.getName();

  @Getter( lazy = true )
  private final String email = ident.getEmailAddress();

  @Getter( lazy = true )
  private final DateValue when = computeWhen();

  private DateValue computeWhen() {
    return DateValue.wrap( ZonedDateTime.ofInstant( ident.getWhenAsInstant(), ident.getZoneId() ) );
  }

  @Override
  public String toString() {
    return getName();
  }

}
