package io.github.atos_digital_id.paprika.history;

import org.eclipse.jgit.lib.ObjectId;

import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.NonNull;

/**
 * Last tag state.
 **/
@Data
public class LastTagState {

  /**
   * Commit id of the last tag.
   *
   * @return the commit id.
   **/
  @NonNull
  private final ObjectId id;

  /**
   * Name of the commit of the last tag.
   *
   * @return the name of the commit.
   **/
  @NonNull
  private final String refName;

  /**
   * Version extracted from the name of the last tag.
   *
   * @return the version.
   **/
  @NonNull
  private final Version version;

}
