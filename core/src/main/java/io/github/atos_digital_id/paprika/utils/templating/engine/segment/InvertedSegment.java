package io.github.atos_digital_id.paprika.utils.templating.engine.segment;

import java.util.List;

import io.github.atos_digital_id.paprika.utils.templating.engine.Context;
import io.github.atos_digital_id.paprika.utils.templating.engine.Template;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.CustomList;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.InvertibleLambda;
import lombok.Data;

@Data
public class InvertedSegment implements Segment {

  private final String key;

  private final List<Segment> segments;

  @Override
  public void execute( TemplateConfig config, Context context, StringBuilder out ) {

    Object value = context.fetch( key );

    if( value == null ) {

      Segment.execute( segments, config, context, out );

    } else if( value instanceof Boolean ) {

      boolean casted = (Boolean) value;
      if( !casted )
        Segment.execute( segments, config, context, out );

    } else if( value instanceof Number ) {

      double casted = ( (Number) value ).doubleValue();
      if( casted == 0d )
        Segment.execute( segments, config, context, out );

    } else if( value instanceof String ) {

      String casted = (String) value;
      if( casted.isEmpty() )
        Segment.execute( segments, config, context, out );

    } else if( value instanceof List ) {

      List<?> casted = (List<?>) value;
      if( casted.isEmpty() )
        Segment.execute( segments, config, context, out );

    } else if( value instanceof CustomList ) {

      CustomList casted = (CustomList) value;
      if( casted.size() == 0 )
        Segment.execute( segments, config, context, out );

    } else if( value instanceof InvertibleLambda ) {

      InvertibleLambda casted = (InvertibleLambda) value;
      Template template = new Template( config, segments, context );
      casted.invertExecute( template, out );

    }

  }

}
