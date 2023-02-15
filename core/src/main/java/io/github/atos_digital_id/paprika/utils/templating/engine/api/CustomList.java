package io.github.atos_digital_id.paprika.utils.templating.engine.api;

/**
 * Interface for iterable data.
 */
public interface CustomList {

  /**
   * Returns the number of elements.
   *
   * @return the number of elements.
   */
  public int size();

  /**
   * Returns the element at the specified position.
   *
   * @param index the position of the element.
   * @return the element at the specified position.
   */
  public Object get( int index );

}
