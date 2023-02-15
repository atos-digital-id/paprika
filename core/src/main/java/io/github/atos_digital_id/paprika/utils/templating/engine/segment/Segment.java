package io.github.atos_digital_id.paprika.utils.templating.engine.segment;

import java.util.List;

import io.github.atos_digital_id.paprika.utils.templating.engine.Context;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;

public interface Segment {

  public static void execute(
      List<Segment> segments,
      TemplateConfig config,
      Context context,
      StringBuilder out ) {

    for( Segment segment : segments )
      segment.execute( config, context, out );

  }

  public void execute( TemplateConfig config, Context context, StringBuilder out );

}
