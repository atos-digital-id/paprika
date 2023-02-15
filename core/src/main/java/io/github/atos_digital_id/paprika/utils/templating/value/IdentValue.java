package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import java.time.ZonedDateTime;

import org.eclipse.jgit.lib.PersonIdent;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrap a Git identity.
 */
@Data
public class IdentValue {

  /**
   * Return a wrapper around an identity.
   *
   * @param ident the identity.
   * @return the wrapped identity, or {@code null} if the identity is
   *         {@code null}.
   */
  public static IdentValue wrap( PersonIdent ident ) {
    return ident == null ? null : new IdentValue( ident );
  }

  @NonNull
  @Getter( NONE )
  private final PersonIdent ident;

  /**
   * The name of the identity.
   *
   * @return the name.
   */
  @Getter( lazy = true )
  private final String name = ident.getName();

  /**
   * The email of the identity.
   *
   * @return the email.
   */
  @Getter( lazy = true )
  private final String email = ident.getEmailAddress();

  /**
   * The date of the identity.
   *
   * @return the date.
   */
  @Getter( lazy = true )
  private final DateValue when = computeWhen();

  private DateValue computeWhen() {
    return DateValue.wrap( ZonedDateTime.ofInstant( ident.getWhenAsInstant(), ident.getZoneId() ) );
  }

  /**
   * Return the name of the identity.
   *
   * @return the name.
   * @see getName
   */
  @Override
  public String toString() {
    return getName();
  }

}
