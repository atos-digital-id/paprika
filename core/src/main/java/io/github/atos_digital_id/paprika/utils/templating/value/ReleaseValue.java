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

@Data
public class ReleaseValue {

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

  private final String name;

  private final VersionValue version;

  @Getter( lazy = true )
  private final boolean released = name != null && !name.isEmpty();

  private final TagValue tag;

  @Getter( lazy = true )
  private final boolean tagged = tag != null;

  public static class ChangesValue extends AbstractCustomList<CommitValue> {

    public ChangesValue( List<CommitValue> changes ) {
      super( changes, System.lineSeparator() );
    }

  }

  private final ChangesValue changes;

  @Override
  public String toString() {
    return Objects.toString( version, "" );
  }

}
