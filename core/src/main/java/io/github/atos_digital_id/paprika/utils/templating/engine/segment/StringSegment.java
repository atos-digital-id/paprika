package io.github.atos_digital_id.paprika.utils.templating.engine.segment;

import io.github.atos_digital_id.paprika.utils.templating.engine.Context;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;
import lombok.Data;

@Data
public class StringSegment implements Segment {

  private final String text;

  @Override
  public void execute( TemplateConfig config, Context context, StringBuilder out ) {
    out.append( text );
  }

}
