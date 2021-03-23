package io.github.atos_digital_id.paprika.utils.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import io.github.atos_digital_id.paprika.project.ArtifactId;
import lombok.NonNull;

/**
 * Implementation of {@link ArtifactIdCache} based on {@link HashMap}.
 **/
public class HashMapArtifactIdCache<STATE> implements ArtifactIdCache<STATE> {

  private final Map<ArtifactId, STATE> states = new HashMap<>();

  @Override
  public STATE get( @NonNull ArtifactId id, @NonNull Supplier<STATE> supplier ) {

    STATE state = states.get( id );

    if( state == null ) {
      state = supplier.get();
      if( state == null )
        throw new IllegalArgumentException( "Can not store a null value." );
      states.put( id, state );
    }

    return state;

  }

  @Override
  public Optional<STATE> peek( @NonNull ArtifactId id ) {
    return Optional.ofNullable( states.get( id ) );
  }

  @Override
  public STATE set( @NonNull ArtifactId id, @NonNull STATE state ) {
    states.put( id, state );
    return state;
  }

}
