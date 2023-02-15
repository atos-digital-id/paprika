package io.github.atos_digital_id.paprika.utils.templating.value;

import static lombok.AccessLevel.NONE;

import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;

import io.github.atos_digital_id.paprika.GitHandler;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;

@Data
public class TagValue {

  public static TagValue wrap( @NonNull GitHandler git, RevObject obj ) {
    return obj != null && obj instanceof RevTag ? new TagValue( git, (RevTag) obj ) : null;
  }

  @NonNull
  @Getter( NONE )
  private GitHandler git;

  @NonNull
  @Getter( NONE )
  private final RevTag tag;

  @Getter( lazy = true )
  private final IdentValue tagger = IdentValue.wrap( tag.getTaggerIdent() );

  @Getter( lazy = true )
  private final DateValue when = computeWhen();

  private DateValue computeWhen() {
    IdentValue taggerIdent = getTagger();
    return taggerIdent == null ? DateValue.wrap( git.startTime() ) : taggerIdent.getWhen();
  }

  @Getter( lazy = true )
  private final CommitMessageValue message = CommitMessageValue.wrap( tag.getFullMessage() );

  @Override
  public String toString() {
    return tag.getTagName();
  }

}
