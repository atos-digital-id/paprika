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

@Data
public class CommitValue {

  /*
   * Commit extractions
   */

  public static String getNameOf( RevCommit commit ) {
    return ObjectId.toString( commit );
  }

  public static String getPrettyNameOf( RevCommit commit ) {
    return getNameOf( commit ).substring( 0, 9 );
  }

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

  public static String getStringDateOf( @NonNull GitHandler git, RevCommit commit ) {
    return getDateOf( git, commit ).format( DATE_TIME_FORMATTER );
  }

  /*
   * Wrap
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

  @Getter( lazy = true )
  private final String id = getNameOf( commit );

  @Getter( lazy = true )
  private final String shortId = getPrettyNameOf( commit );

  @Getter( lazy = true )
  private final IdentValue author = IdentValue.wrap( commit.getAuthorIdent() );

  @Getter( lazy = true )
  private final IdentValue committer = IdentValue.wrap( commit.getCommitterIdent() );

  @Getter( lazy = true )
  private final DateValue when = DateValue.wrap( getDateOf( git, commit ) );

  @Getter( lazy = true )
  private final CommitMessageValue message = CommitMessageValue.wrap( commit.getFullMessage() );

  @Override
  public String toString() {
    return getShortId();
  }

}
