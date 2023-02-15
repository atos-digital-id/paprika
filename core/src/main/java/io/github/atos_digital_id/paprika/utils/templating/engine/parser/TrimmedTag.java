package io.github.atos_digital_id.paprika.utils.templating.engine.parser;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;

@Data
public class TrimmedTag {

  @With
  @NonNull
  private final Tag tag;

  @Getter( lazy = true )
  private final TagType type = tag.getType();

  @Getter( lazy = true )
  private final String data = tag.getData();

  public TrimmedTag withData( String data ) {
    return withTag( tag.withData( data ) );
  }

  @Getter( lazy = true )
  private final Position start = tag.getStart();

  @Getter( lazy = true )
  private final Position end = tag.getEnd();

  @NonNull
  private final String before;

  @NonNull
  private final String after;

  public TrimmedTag withTrimmed( String before, String after ) {
    return new TrimmedTag( tag, before, after );
  }

}
