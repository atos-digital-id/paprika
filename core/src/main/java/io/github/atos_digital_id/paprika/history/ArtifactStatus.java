package io.github.atos_digital_id.paprika.history;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.lib.ObjectId;

import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Complete status of a module.
 **/
@Data
public class ArtifactStatus {

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ssXXX" );

  /**
   * Commit id of the last modifying commit.
   *
   * @return the commit id of the last modifying commit.
   **/
  @NonNull
  private final ObjectId lastCommit;

  /**
   * String representation of {@link ArtifactStatus#getLastCommit}.
   *
   * @return the string representation of the last modifying commit.
   **/
  @Getter( lazy = true )
  private final String lastCommitAsString = this.lastCommit.name();

  /**
   * Short string representation of {@link ArtifactStatus#getLastCommit}.
   *
   * @return the short string representation of the last modifying commit.
   **/
  @Getter( lazy = true )
  private final String lastCommitAsShortString = Pretty.id( this.lastCommit ).toString();

  /**
   * Date of creation of {@link ArtifactStatus#getLastCommit}.
   *
   * @return the date of creation of the last modifying commit.
   **/
  @NonNull
  private final ZonedDateTime lastModification;

  /**
   * String representation of {@link ArtifactStatus#getLastModification}.
   *
   * @return the string representation of the date of creation of the last
   *         modifying commit.
   **/
  @Getter( lazy = true )
  private final String lastModificationAsString =
      this.lastModification.format( DATE_TIME_FORMATTER );

  /**
   * Commit id of the last tag.
   *
   * @return the commit id of the last tag.
   **/
  @NonNull
  private final ObjectId tagCommit;

  /**
   * String representation of {@link ArtifactStatus#getTagCommit}.
   *
   * @return the string representation of the commit id of the last tag.
   **/
  @Getter( lazy = true )
  private final String tagCommitAsString = this.tagCommit.name();

  /**
   * Short string representation of {@link ArtifactStatus#getTagCommit}.
   *
   * @return the short string representation of the commit id of the last tag.
   **/
  @Getter( lazy = true )
  private final String tagCommitAsShortString = Pretty.id( this.tagCommit ).toString();

  /**
   * Last tag name.
   *
   * @return the last tag name.
   **/
  @NonNull
  private final String refName;

  /**
   * Version extracted from the last tag name.
   *
   * @return the version extracted from the last tag name.
   **/
  @NonNull
  private final Version baseVersion;

  /**
   * String representation of {@link ArtifactStatus#getBaseVersion}.
   *
   * @return the string representation of the version extracted from the last
   *         tag name.
   **/
  @Getter( lazy = true )
  private final String baseVersionAsString = this.baseVersion.toString();

  /**
   * Snapshot flag. A module is a snapshot if it's dirty or modified since the
   * last tag.
   *
   * @return the snapshot flag.
   **/
  private final boolean snapshot;

  /**
   * String representation of {@link ArtifactStatus#isSnapshot}.
   *
   * @return the string representation of the snapshot flag.
   **/
  @Getter( lazy = true )
  private final String snapshotAsString = Boolean.toString( this.snapshot );

  /**
   * Pristine flag. A module is pristine if it has not been modified since the
   * last tag.
   *
   * @return the pristine flag.
   **/
  public boolean isPristine() {
    return !this.snapshot;
  }

  /**
   * String representation of {@link ArtifactStatus#isPristine}.
   *
   * @return the string representation of the pristine flag.
   **/
  @Getter( lazy = true )
  private final String pristineAsString = Boolean.toString( isPristine() );

  /**
   * Computed current version.
   *
   * @return the computed current version.
   **/
  @NonNull
  private final Version version;

  /**
   * String representation of {@link ArtifactStatus#getVersion}.
   *
   * @return the string representation of the computed current version.
   **/
  @Getter( lazy = true )
  private final String versionAsString = this.version.toString();

}
