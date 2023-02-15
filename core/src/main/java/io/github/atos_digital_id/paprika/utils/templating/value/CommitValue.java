package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revwalk.RevCommit;

import io.github.atos_digital_id.paprika.GitHandler;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrapper around a Git commit.
 */
@Data
public class CommitValue {

  /*
   * Commit extractions
   */

  /**
   * Get the complete name (hex string representation) of a commit.
   *
   * @param commit the commit.
   * @return the hex string identifying the commit.
   */
  public static String getNameOf( RevCommit commit ) {
    return ObjectId.toString( commit );
  }

  /**
   * Get the 9 first characters of the name of the commit.
   *
   * @param commit the commit.
   * @return the 9 first characters of the name.
   * @see getNameOf
   */
  public static String getPrettyNameOf( RevCommit commit ) {
    return getNameOf( commit ).substring( 0, 9 );
  }

  /**
   * Extract a date from a commit. If the commit contains a commiter, extract
   * the date from the commiter. If the commit contains an author, extract the
   * date from the author. If the commit is {@code null} or no commiter nor
   * author is found, return the starting date of the Maven command.
   *
   * @param git the {@link GitHandler} singleton.
   * @param commit the commit.
   * @return the date of the commit.
   */
  public static ZonedDateTime getDateOf( @NonNull GitHandler git, RevCommit commit ) {

    if( commit == null )
      return git.startTime();

    PersonIdent committer = commit.getCommitterIdent();
    if( committer == null )
      committer = commit.getAuthorIdent();
    if( committer != null )
      return ZonedDateTime.ofInstant( committer.getWhenAsInstant(), committer.getZoneId() );

    return git.startTime();

  }

  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern( "yyyy-MM-dd'T'HH:mm:ssXXX" );

  /**
   * Extract a formated date from a commit. The pattern used is
   * {@code yyyy-MM-dd'T'HH:mm:ssXXX}.
   *
   * @param git the {@link GitHandler} singleton.
   * @param commit the commit.
   * @return the date of the commit.
   * @see getDateOf
   * @see DateTimeFormatter
   */
  public static String getStringDateOf( @NonNull GitHandler git, RevCommit commit ) {
    return getDateOf( git, commit ).format( DATE_TIME_FORMATTER );
  }

  /*
   * Wrap
   */

  /**
   * Wrap a commit.
   *
   * @param git the {@link GitHandler} singleton.
   * @param commit the commit to wrap.
   * @return the wrapper around the commit, or {@code null} if the commit is
   *         {@code null}.
   */
  public static CommitValue wrap( @NonNull GitHandler git, RevCommit commit ) {
    return commit == null ? null : new CommitValue( git, commit );
  }

  @NonNull
  @Getter( NONE )
  private GitHandler git;

  @NonNull
  @Getter( NONE )
  private final RevCommit commit;

  /**
   * The name (hex string) of the commit.
   *
   * @return the name of the commit.
   * @see getNameOf
   */
  @Getter( lazy = true )
  private final String id = getNameOf( commit );

  /**
   * The shorten name of the commit.
   *
   * @return the shorten name of the commit.
   * @see getPrettyNameOf
   */
  @Getter( lazy = true )
  private final String shortId = getPrettyNameOf( commit );

  /**
   * The author of the commit.
   *
   * @return the author of the commit.
   */
  @Getter( lazy = true )
  private final IdentValue author = IdentValue.wrap( commit.getAuthorIdent() );

  /**
   * The commiter of the commit.
   *
   * @return the commiter of the commit.
   */
  @Getter( lazy = true )
  private final IdentValue committer = IdentValue.wrap( commit.getCommitterIdent() );

  /**
   * The date of the commit.
   *
   * @return the date of the commit.
   * @see getDateOf
   */
  @Getter( lazy = true )
  private final DateValue when = DateValue.wrap( getDateOf( git, commit ) );

  /**
   * The message of the commit.
   *
   * @return the message of the commit.
   */
  @Getter( lazy = true )
  private final CommitMessageValue message = CommitMessageValue.wrap( commit.getFullMessage() );

  /**
   * Return the shorten id of the commit.
   *
   * @return the shorten id of the commit.
   * @see getShortId
   */
  @Override
  public String toString() {
    return getShortId();
  }

}
