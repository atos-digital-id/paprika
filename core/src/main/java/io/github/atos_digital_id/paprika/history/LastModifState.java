package io.github.atos_digital_id.paprika.history;

import org.eclipse.jgit.revwalk.RevCommit;

import lombok.Data;

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
   * Commit of the last modification, {@code null} if dirty.
   *
   * @return the last modifying commit.
   **/
  private final RevCommit commit;

}
