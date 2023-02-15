package io.github.atos_digital_id.paprika.history;

import org.eclipse.jgit.revwalk.RevCommit;

import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;

/**
 * Last tag state.
 **/
@Data
public class LastTagState {

  /**
   * Commit of the last tag, {@code null} if never tagged.
   *
   * @return the last tagged commit.
   **/
  private final RevCommit commit;

  /**
   * Last tag, {@code null} if never tagged.
   *
   * @return the last tag.
   **/
  private final String refName;

  /**
   * Version extracted from the last tag, {code null} if never tagged.
   *
   * @return the version.
   **/
  private final Version version;

}
