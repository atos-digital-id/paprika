package io.github.atos_digital_id.paprika.utils.templating.engine.segment;

import java.util.List;
import java.util.regex.Pattern;

import io.github.atos_digital_id.paprika.utils.templating.engine.Context;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;
import io.github.atos_digital_id.paprika.utils.templating.engine.parser.Parser;
import lombok.Data;

@Data
public class PartialSegment implements Segment {

  private static final Pattern INDENT_POSITION = Pattern.compile( "\\A|\\R(?!\\z)" );

  private final String indent;

  private final String key;

  @Override
  public void execute( TemplateConfig config, Context context, StringBuilder out ) {

    String source = config.getPartialsLoader().load( key );
    if( source == null || source.isEmpty() )
      return;
    if( !indent.isEmpty() )
      source = INDENT_POSITION.matcher( source ).replaceAll( "$0" + indent );

    List<Segment> segments = Parser.parse( source );
    Segment.execute( segments, config, context, out );

  }

}
