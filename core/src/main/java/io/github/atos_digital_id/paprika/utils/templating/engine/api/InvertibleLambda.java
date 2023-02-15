package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import io.github.atos_digital_id.paprika.utils.templating.engine.Template;

/**
 * Interface for callable inverted lambdas.
 */
public interface InvertibleLambda extends Lambda {

  /**
   * Executes the inverted lambda on supplied template. The lambda should write
   * its results in {@code out}.
   *
   * @param template the template passed to the lambda.
   * @param out where to write to output of the lambda.
   */
  public void invertExecute( Template template, StringBuilder out );

}
