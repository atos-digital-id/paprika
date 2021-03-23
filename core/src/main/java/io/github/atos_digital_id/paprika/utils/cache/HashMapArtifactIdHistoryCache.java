package io.github.atos_digital_id.paprika.utils.cache;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.eclipse.jgit.lib.ObjectId;

import io.github.atos_digital_id.paprika.project.ArtifactId;
import lombok.NonNull;

/**
 * Implementation of {@link ArtifactIdHistoryCache} based on {@link HashMap}.
 **/
public class HashMapArtifactIdHistoryCache<STATE> implements ArtifactIdHistoryCache<STATE> {

  private final Map<ArtifactId, Map<ObjectId, STATE>> states = new HashMap<>();

  @Override
  public STATE get(
      @NonNull ArtifactId id,
      @NonNull ObjectId commit,
      @NonNull Supplier<STATE> supplier ) {

    Map<ObjectId, STATE> map = states.computeIfAbsent( id, d -> new HashMap<>() );
    STATE state = map.get( commit );

    if( state == null ) {
      state = supplier.get();
      if( state == null )
        throw new IllegalArgumentException( "Can not store a null value." );
      map.put( commit, state );
    }

    return state;

  }

  @Override
  public Optional<STATE> peek( @NonNull ArtifactId id, @NonNull ObjectId commit ) {
    Map<ObjectId, STATE> map = states.get( id );
    return map == null ? Optional.empty() : Optional.ofNullable( map.get( commit ) );
  }

  @Override
  public STATE set( @NonNull ArtifactId id, @NonNull ObjectId commit, @NonNull STATE state ) {
    states.computeIfAbsent( id, d -> new HashMap<>() ).put( commit, state );
    return state;
  }

}
