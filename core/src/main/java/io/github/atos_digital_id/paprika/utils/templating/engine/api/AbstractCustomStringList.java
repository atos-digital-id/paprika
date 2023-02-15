package io.github.atos_digital_id.paprika.utils.templating.engine.api;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

public class AbstractCustomStringList extends AbstractCustomList<String> implements CustomMap {

  protected final UnaryOperator<String> canonic;

  protected final Set<?> set;

  public AbstractCustomStringList(
      List<String> list,
      String separator,
      UnaryOperator<String> canonic ) {

    super( list.stream().map( canonic ).collect( Collectors.toList() ), separator );
    this.canonic = canonic;
    this.set = new HashSet<>( this.list );

  }

  @Override
  public boolean containsKey( String key ) {
    return set.contains( canonic.apply( key ) );
  }

  @Override
  public Boolean get( String key ) {
    return containsKey( key );
  }

}
