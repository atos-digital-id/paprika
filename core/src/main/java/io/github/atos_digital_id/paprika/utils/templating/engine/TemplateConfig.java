package io.github.atos_digital_id.paprika.utils.templating.engine;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.With;

@Data
@Builder
public class TemplateConfig {

  @NonNull
  @Builder.Default
  @With
  private final Escaper escaper = Escaper.HTML;

  public static interface PartialsLoader {

    public String load( String path );

  }

  @NonNull
  @Builder.Default
  @With
  private final PartialsLoader partialsLoader = defaultPartialsLoader();

  private static PartialsLoader defaultPartialsLoader() {
    return path -> "";
  }

}
