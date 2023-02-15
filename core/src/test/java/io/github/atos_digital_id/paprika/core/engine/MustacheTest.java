package io.github.atos_digital_id.paprika.core.engine;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import io.github.atos_digital_id.paprika.core.engine.MustacheTest.TestData.TestDataBuilder;
import io.github.atos_digital_id.paprika.utils.templating.engine.Escaper;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateConfig;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateEngine;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Singular;

public class MustacheTest {

  @Data
  @Builder
  public static class TestData {

    @NonNull
    @Builder.Default
    private final Escaper escaper = Escaper.HTML;

    @Singular
    private final Map<String, String> partials;

    @Singular( "data" )
    private final Map<String, Object> data;

    @NonNull
    @Builder.Default
    private final String template = "";

    @NonNull
    @Builder.Default
    private final String expected = "";

    public static class TestDataBuilder {

      public TestDataBuilder partialJoin( String key, String ... strings ) {
        return this.partial( key, String.join( "\n", strings ) + "\n" );
      }

      public TestDataBuilder templateJoin( String ... strings ) {
        return this.template( String.join( "\n", strings ) + "\n" );
      }

      public TestDataBuilder expectedJoin( String ... strings ) {
        return this.expected( String.join( "\n", strings ) + "\n" );
      }

    }

  }

  public static void test( Consumer<TestDataBuilder> setter ) {

    TestDataBuilder builder = TestData.builder();
    setter.accept( builder );
    TestData data = builder.build();

    Map<String, String> partials = data.getPartials();

    TemplateConfig config = TemplateConfig.builder().escaper( data.getEscaper() )
        .partialsLoader( key -> partials.get( key ) ).build();

    Map<String, Object> map = data.getData();
    if( map == null )
      map = new HashMap<>();

    assertThat( TemplateEngine.execute( config, data.getTemplate(), map ) )
        .isEqualTo( data.getExpected() );

  }

}
