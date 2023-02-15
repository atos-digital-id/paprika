package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import io.github.atos_digital_id.paprika.utils.templating.engine.Template;

public interface InvertibleLambda extends Lambda {

  public void invertExecute( Template template, StringBuilder out );

}
