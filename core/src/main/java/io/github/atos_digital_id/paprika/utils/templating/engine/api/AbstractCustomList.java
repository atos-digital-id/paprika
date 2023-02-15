package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import java.util.List;
import java.util.Objects;

public abstract class AbstractCustomList<T> implements CustomList {

  protected final List<T> list;

  protected final String sep;

  public AbstractCustomList( List<T> list, String sep ) {
    this.list = list;
    this.sep = sep;
  }

  @Override
  public int size() {
    return list.size();
  }

  @Override
  public T get( int index ) {
    return list.get( index );
  }

  @Override
  public boolean equals( Object obj ) {
    if( this == obj )
      return true;
    if( obj == null )
      return false;
    if( obj instanceof CustomList ) {
      CustomList casted = (CustomList) obj;
      if( this.size() != casted.size() )
        return false;
      for( int i = 0; i < this.size(); i++ )
        if( !Objects.equals( this.get( i ), casted.get( i ) ) )
          return false;
      return true;
    }
    return list.equals( obj );
  }

  @Override
  public int hashCode() {
    return list.hashCode();
  }

  @Override
  public String toString() {

    if( list.isEmpty() )
      return "";

    StringBuilder out = new StringBuilder();

    out.append( Objects.toString( get( 0 ), "" ) );

    for( int i = 1; i < list.size(); i++ )
      out.append( sep ).append( Objects.toString( get( i ), "" ) );

    return out.toString();

  }

}
