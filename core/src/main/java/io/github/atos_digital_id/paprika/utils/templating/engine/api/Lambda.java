package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import io.github.atos_digital_id.paprika.utils.templating.engine.Template;

public interface Lambda {

  public void execute( Template template, StringBuilder out );

}
