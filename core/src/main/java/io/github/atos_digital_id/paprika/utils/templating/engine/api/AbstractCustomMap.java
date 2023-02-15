package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import java.util.Iterator;
import java.util.Map;
import java.util.function.UnaryOperator;

public class AbstractCustomMap<T> implements CustomMap {

  protected final Map<String, T> map;

  protected final UnaryOperator<String> canonic;

  protected final String keySep;

  protected final String valSep;

  public AbstractCustomMap(
      Map<String, T> map,
      UnaryOperator<String> canonic,
      String keySep,
      String valSep ) {

    this.map = map;
    this.canonic = canonic;
    this.keySep = keySep;
    this.valSep = valSep;

  }

  @Override
  public boolean containsKey( String k ) {
    return map.containsKey( canonic.apply( k ) );
  }

  @Override
  public T get( String k ) {
    return map.get( canonic.apply( k ) );
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj )
      return true;
    if( obj == null )
      return false;
    if( obj instanceof AbstractCustomMap ) {
      AbstractCustomMap<?> casted = (AbstractCustomMap<?>) obj;
      return this.map.equals( casted.map );
    }
    return map.equals( obj );
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  @Override
  public String toString() {

    if( map.isEmpty() )
      return "";

    StringBuilder out = new StringBuilder();

    Iterator<String> ite = map.keySet().iterator();
    if( ite.hasNext() ) {
      String key = ite.next();
      out.append( key ).append( keySep ).append( map.get( key ) );
    }
    while( ite.hasNext() ) {
      String key = ite.next();
      out.append( valSep ).append( key ).append( keySep ).append( map.get( key ) );
    }

    return out.toString();

  }

}
