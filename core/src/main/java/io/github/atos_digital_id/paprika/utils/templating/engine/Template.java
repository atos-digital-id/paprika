package io.github.atos_digital_id.paprika.utils.templating.engine;

import java.util.List;

import io.github.atos_digital_id.paprika.utils.templating.engine.segment.Segment;
import lombok.Data;
import lombok.NonNull;

@Data
public class Template {

  @NonNull
  private final TemplateConfig config;

  @NonNull
  private final List<Segment> segments;

  private final Context context;

  public String execute() {
    StringBuilder out = new StringBuilder();
    execute( out );
    return out.toString();
  }

  public void execute( StringBuilder out ) {
    Segment.execute( segments, config, context, out );
  }

}
