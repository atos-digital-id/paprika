package io.github.atos_digital_id.paprika.utils.templating.engine.segment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import io.github.atos_digital_id.paprika.utils.templating.engine.Context;
import io.github.atos_digital_id.paprika.utils.templating.engine.Template;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.CustomList;
import io.github.atos_digital_id.paprika.utils.templating.engine.api.Lambda;
import lombok.Data;

@Data
public class SectionSegment implements Segment {

  private final String key;

  private final List<Segment> segments;

  @Override
  public void execute( TemplateConfig config, Context context, StringBuilder out ) {

    Object value = context.fetch( key );

    if( value == null )
      return;

    if( value instanceof Boolean ) {

      boolean casted = (Boolean) value;
      if( casted )
        Segment.execute( segments, config, context, out );

    } else if( value instanceof Number ) {

      double casted = ( (Number) value ).doubleValue();
      if( casted != 0d )
        Segment.execute( segments, config, context.sub( value ), out );

    } else if( value instanceof String ) {

      String casted = (String) value;
      if( !casted.isEmpty() )
        Segment.execute( segments, config, context.sub( casted ), out );

    } else if( value instanceof List ) {

      List<?> casted = (List<?>) value;
      executeIterable( config, context, casted.size(), i -> casted.get( i ), out );

    } else if( value instanceof CustomList ) {

      CustomList casted = (CustomList) value;
      executeIterable( config, context, casted.size(), i -> casted.get( i ), out );

    } else if( value instanceof Lambda ) {

      Lambda casted = (Lambda) value;
      Template template = new Template( config, segments, context );
      casted.execute( template, out );

    } else {

      Segment.execute( segments, config, context.sub( value ), out );

    }

  }

  private void executeIterable(
      TemplateConfig config,
      Context base,
      int size,
      Function<Integer, Object> getter,
      StringBuilder out ) {

    for( int i = 0; i < size; i++ ) {

      Object value = getter.apply( i );

      Map<String, Object> add = new HashMap<>();
      add.put( "@first", i == 0 );
      add.put( "@last", i == ( size - 1 ) );
      add.put( "@index", i );
      add.put( "@indexPlusOne", i + 1 );
      add.put( "@indexIsEven", i % 2 == 0 );

      Context sub = base.sub( value, add );

      Segment.execute( segments, config, sub, out );

    }

  }

}
