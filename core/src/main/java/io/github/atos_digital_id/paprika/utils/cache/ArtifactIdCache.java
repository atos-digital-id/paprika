package io.github.atos_digital_id.paprika.utils.cache;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.atos_digital_id.paprika.project.ArtifactId;

/**
 * Cache interface for ids.
 **/
public interface ArtifactIdCache<STATE> {

  /**
   * Retreive the value associated with the id, or call the supplier if the
   * value has not been computed yet.
   *
   * @param id the input id.
   * @param supplier the supplier of value to use.
   * @return the associated value.
   **/
  public STATE get( ArtifactId id, Supplier<STATE> supplier );

  /**
   * Returns the value associated with id, if is computed already.
   *
   * @param id the input id.
   * @return the associated value.
   **/
  public Optional<STATE> peek( ArtifactId id );

  /**
   * Affects directly a value to an id. On the contrary of {@link Map#put}, the
   * returned value is the given value.
   *
   * @param id the input id.
   * @param state the associated value.
   * @return the input state.
   **/
  public STATE set( ArtifactId id, STATE state );

}
