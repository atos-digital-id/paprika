package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;

import io.github.atos_digital_id.paprika.GitHandler;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

/**
 * Wrapper around an annotation commit.
 */
@Data
public class TagValue {

  /**
   * Return a wrapper around an annotation commit.
   *
   * @param git the {@link GitHandler} singleton.
   * @param obj the annotation commit.
   */
  public static TagValue wrap( @NonNull GitHandler git, RevObject obj ) {
    return obj != null && obj instanceof RevTag ? new TagValue( git, (RevTag) obj ) : null;
  }

  @NonNull
  @Getter( NONE )
  private GitHandler git;

  @NonNull
  @Getter( NONE )
  private final RevTag tag;

  /**
   * Tagger identity.
   *
   * @return the tagger of the annotation.
   */
  @Getter( lazy = true )
  private final IdentValue tagger = IdentValue.wrap( tag.getTaggerIdent() );

  /**
   * Date of the annotation.
   *
   * @return the date of the annotation.
   */
  @Getter( lazy = true )
  private final DateValue when = computeWhen();

  private DateValue computeWhen() {
    IdentValue taggerIdent = getTagger();
    return taggerIdent == null ? DateValue.wrap( git.startTime() ) : taggerIdent.getWhen();
  }

  /**
   * Message of the annotation commit.
   *
   * @return the message.
   */
  @Getter( lazy = true )
  private final CommitMessageValue message = CommitMessageValue.wrap( tag.getFullMessage() );

  /**
   * Return the tag name.
   *
   * @return the tag name.
   */
  @Override
  public String toString() {
    return tag.getTagName();
  }

}
