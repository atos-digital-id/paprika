package io.github.atos_digital_id.paprika.utils.templating.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.AbstractCustomList;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Return a wrapper around a release. A release is defined by its name and the
 * commits since the precedent release. The last commits of the Git history can
 * be grouped in an anonymous release with an empty name.
 */
@Data
public class ReleaseValue {

  /**
   * Create a release wrapper.
   *
   * @param git the {@link GitHandler} singleton.
   * @param tagName the tag name closing the release.
   * @param version the version extracted from the tag.
   * @param taggedObj the annotation commit, if any.
   * @param commits the commits of the release.
   * @return the wrapped release.
   */
  public static ReleaseValue wrap(
      @NonNull GitHandler git,
      String tagName,
      Version version,
      RevObject taggedObj,
      List<RevCommit> commits ) {

    List<CommitValue> changes = new ArrayList<>( commits.size() );
    for( RevCommit commit : commits )
      changes.add( CommitValue.wrap( git, commit ) );

    return new ReleaseValue(
        tagName,
        VersionValue.wrap( version ),
        TagValue.wrap( git, taggedObj ),
        new ChangesValue( changes ) );

  }

  /**
   * Name of the release. It is the complete tag closing the release. If the
   * release is anonymous (for commits not yet released), the name is empty.
   *
   * @return the name of the release.
   */
  private final String name;

  /**
   * Version extracted from the name of the release.
   *
   * @return the version of the release.
   */
  private final VersionValue version;

  /**
   * Check if the release is not anonymous.
   *
   * @return {@code true} if the name if not null or empty.
   */
  @Getter( lazy = true )
  private final boolean released = name != null && !name.isEmpty();

  /**
   * The annotation commit, if any.
   *
   * @return the annotation commit.
   */
  private final TagValue tag;

  /**
   * Check if the tag is annotated.
   *
   * @return {@code true} if the tag is annotated.
   */
  @Getter( lazy = true )
  private final boolean tagged = tag != null;

  /**
   * Iterable list of commits.
   */
  public static class ChangesValue extends AbstractCustomList<CommitValue> {

    public ChangesValue( List<CommitValue> changes ) {
      super( changes, System.lineSeparator() );
    }

  }

  /**
   * Commits included in the release.
   *
   * @return commits
   */
  private final ChangesValue changes;

  /**
   * Return the version of the release, or an empty string for anonymous
   * releases.
   *
   * @return the version of the release.
   * @see getVersion
   */
  @Override
  public String toString() {
    return Objects.toString( version, "" );
  }

}
