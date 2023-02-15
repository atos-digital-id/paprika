package io.github.atos_digital_id.paprika.utils.templating.engine.api;

/**
 * Interface for dynamic key/value data.
 */
public interface CustomMap {

  /**
   * Returns {@code true} if the key is defined (with a value not {@code null}.
   *
   * @param key the key to check the presence.
   * @return {@code true} if the key is defined.
   */
  public boolean containsKey( String key );

  /**
   * Returns the object associated with the key. The returned object can be
   * {@code null} if the key is not defined.
   *
   * @param key the key of the object.
   * @return the object associated with the key.
   */
  public Object get( String key );

}
