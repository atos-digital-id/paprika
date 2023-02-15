package io.github.atos_digital_id.paprika.utils.templating.engine.parser;

import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.With;

@Data
public class Tag {

  @NonNull
  private final TagType type;

  @Getter( lazy = true )
  private final boolean standalone = type.isStandalone();

  @With
  @NonNull
  private final String data;

  @NonNull
  private final Position start;

  @NonNull
  private final Position end;

}
