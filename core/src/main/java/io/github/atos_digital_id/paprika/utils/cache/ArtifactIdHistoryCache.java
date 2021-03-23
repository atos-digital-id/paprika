package io.github.atos_digital_id.paprika.utils.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jgit.lib.ObjectId;

import io.github.atos_digital_id.paprika.project.ArtifactId;

/**
 * Cache interface for ids and commit id.
 **/
public interface ArtifactIdHistoryCache<STATE> {

  /**
   * Retreive the value associated with the id and commit id, or call the
   * supplier if the value has not been computed yet.
   *
   * @param id the input id.
   * @param commit the commit id.
   * @param supplier the supplier of value to use.
   * @return the associated value.
   **/
  public STATE get( ArtifactId id, ObjectId commit, Supplier<STATE> supplier );

  /**
   * Returns the value associated with id and the commit id, if is computed
   * already.
   *
   * @param id the input id.
   * @param commit the commit id.
   * @return the associated value.
   **/
  public Optional<STATE> peek( ArtifactId id, ObjectId commit );

  /**
   * Affects directly a value to an id and a commit id. On the contrary of
   * {@link Map#put}, the returned value is the given value.
   *
   * @param id the input id.
   * @param commit the commit id.
   * @param state the associated value.
   * @return the input state.
   **/
  public STATE set( ArtifactId id, ObjectId commit, STATE state );

  /**
   * Affects directly the same value to an id and collection of commit ids. On
   * the contrary of {@link Map#put}, the returned value is the given value.
   *
   * @param id the input id.
   * @param commits the commit ids.
   * @param state the associated value.
   * @return the input state.
   **/
  public default STATE set( ArtifactId id, Collection<? extends ObjectId> commits, STATE state ) {

    STATE result = state;
    for( ObjectId commit : commits )
      result = set( id, commit, state );

    return result;

  }

}
