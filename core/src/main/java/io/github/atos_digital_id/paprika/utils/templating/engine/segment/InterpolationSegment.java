package io.github.atos_digital_id.paprika.utils.templating.engine.segment;

import java.util.Objects;

import io.github.atos_digital_id.paprika.utils.templating.engine.Context;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;
import lombok.Data;

@Data
public class InterpolationSegment implements Segment {

  private final String key;

  private final boolean escaped;

  @Override
  public void execute( TemplateConfig config, Context context, StringBuilder out ) {

    String value = Objects.toString( context.fetch( key ), "" );
    if( escaped )
      value = config.getEscaper().escape( value );
    out.append( value );

  }

}
