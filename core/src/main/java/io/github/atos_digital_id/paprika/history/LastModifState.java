package io.github.atos_digital_id.paprika.history;

import java.time.ZonedDateTime;

import org.eclipse.jgit.lib.ObjectId;

import lombok.Data;
import lombok.NonNull;

/**
 * Last modification state.
 **/
@Data
public class LastModifState {

  /**
   * Seniority of the last modification. Number of commit between HEAD and the
   * last modification commit, 0 if dirty.
   *
   * @return the seniority.
   **/
  private final int seniority;

  /**
   * Commit id of the last modification.
   *
   * @return the commit id.
   **/
  @NonNull
  private final ObjectId id;

  /**
   * Name of the commit of the last modification. {@code "HEAD"} if the module
   * is dirty, the short commit id if not.
   *
   * @return the name of the commit.
   **/
  @NonNull
  private final String refName;

  /**
   * Date of the commit of the last modification.
   *
   * @return the date of the commit.
   **/
  @NonNull
  private final ZonedDateTime date;

}
