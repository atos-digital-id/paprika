package io.github.atos_digital_id.paprika.utils.templating.engine;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import io.github.atos_digital_id.paprika.utils.templating.engine.api.CustomMap;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Context {

  private static class KeyNotFound extends Exception {}

  public final Context parent;

  private final Object root;

  private final Map<String, Object> additionals;

  public Object fetch( @NonNull String key ) {

    if( ".".equals( key ) )
      return root;

    return fetch( Arrays.asList( key.split( "\\." ) ) );

  }

  private Object fetch( List<String> keys ) {

    Object current = this.root;

    Iterator<String> ite = keys.iterator();

    if( ite.hasNext() ) {
      String key = ite.next();
      try {
        current = fetch( current, key );
      } catch( KeyNotFound ex ) {
        if( additionals.containsKey( key ) )
          current = additionals.get( key );
        else if( parent != null ) {
          return parent.fetch( keys );
        } else
          return null;
      }
    }

    try {
      while( ite.hasNext() ) {
        current = fetch( current, ite.next() );
      }
    } catch( KeyNotFound ex ) {
      return null;
    }

    return current;

  }

  private static Object fetch( Object obj, @NonNull String key ) throws KeyNotFound {

    if( obj == null )
      throw new KeyNotFound();
    if( key.isBlank() )
      return obj;

    try {
      Method method = obj.getClass().getMethod( key );
      return method.invoke( obj );
    } catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {}

    String upper = Character.toUpperCase( key.charAt( 0 ) ) + key.substring( 1 );

    try {
      Method method = obj.getClass().getMethod( "get" + upper );
      return method.invoke( obj );
    } catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {}

    try {
      Method method = obj.getClass().getMethod( "is" + upper );
      return method.invoke( obj );
    } catch( NoSuchMethodException | InvocationTargetException | IllegalAccessException ex ) {}

    if( obj instanceof Map ) {
      Map<?, ?> casted = (Map<?, ?>) obj;
      if( casted.containsKey( key ) )
        return casted.get( key );
    }

    if( obj instanceof CustomMap ) {
      CustomMap casted = (CustomMap) obj;
      if( casted.containsKey( key ) )
        return casted.get( key );
    }

    throw new KeyNotFound();

  }

  public Context sub( Object sub ) {
    return new Context( this, sub, new HashMap<>() );
  }

  public Context sub( Object sub, Map<String, Object> additionals ) {
    return new Context( this, sub, additionals );
  }

}
