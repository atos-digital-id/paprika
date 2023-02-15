package io.github.atos_digital_id.paprika.utils.templating.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.utils.templating.engine.segment.Segment;
import io.github.atos_digital_id.paprika.utils.templating.engine.parser.Parser;

public class TemplateEngine {

  public static String execute( Config config, String source, Object root ) {

    return execute(
        new TemplateConfig( config.getEscaper(), name -> config.getPartialTemplate( name ) ),
        source,
        root );

  }

  public static String execute( TemplateConfig config, String source, Object root ) {

    List<Segment> segments = Parser.parse( source );

    Map<String, Object> additionals = new HashMap<>();
    Context context = new Context( null, root, additionals );

    Template template = new Template( config, segments, context );

    return template.execute();

  }

}
