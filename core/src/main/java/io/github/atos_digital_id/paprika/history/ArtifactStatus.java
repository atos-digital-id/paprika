package io.github.atos_digital_id.paprika.history;

import org.eclipse.jgit.revwalk.RevCommit;

import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.NonNull;

/**
 * Complete status of a module.
 **/
@Data
public class ArtifactStatus {

  /**
   * Commit of the last modifying commit, {@code null} if dirty.
   *
   * @return the last modifying commit.
   **/
  private final RevCommit lastCommit;

  /**
   * Commit id of the last tag, {@code null} if never tagged.
   *
   * @return the last tagged commit.
   **/
  private final RevCommit tagCommit;

  /**
   * Last tag name, {@code null} if never tagged.
   *
   * @return the last tag name.
   **/
  private final String refName;

  /**
   * Version extracted from the last tag name, {@code null} if never tagged.
   *
   * @return the version extracted from the last tag name.
   **/
  private final Version baseVersion;

  /**
   * Computed current version.
   *
   * @return the computed current version.
   **/
  @NonNull
  private final Version version;

}
