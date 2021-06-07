package io.github.atos_digital_id.paprika.utils;

import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.Ref;

/**
 * Utility class to format some objects. Returned objects implements a lazy
 * {code toString} method.
 **/
public class Pretty {

  private Pretty() {}

  /**
   * Pretty formatter interface.
   **/
  @FunctionalInterface
  public interface PrettyFun<T> {

    /**
     * Format an object and append the result in a builder.
     *
     * @param builder the builder to appened.
     * @param obj the object to format.
     **/
    public void format( StringBuilder builder, T obj );

  }

  private static <T> Object lazy( PrettyFun<T> prettyFun, T obj ) {

    return new Object() {

      private boolean formatted;

      private String string;

      @Override
      public String toString() {

        if( formatted )
          return string;

        StringBuilder builder = new StringBuilder();
        prettyFun.format( builder, obj );
        string = builder.toString();

        formatted = true;
        return string;

      }

    };

  }

  private static <T> PrettyFun<Collection<? extends T>> coll( PrettyFun<T> prettyFun ) {

    return ( builder, collection ) -> {

      if( collection == null ) {
        builder.append( "null" );
        return;
      }
      if( collection.isEmpty() ) {
        builder.append( "[]" );
        return;
      }

      builder.append( "[ " );
      Iterator<? extends T> ite = collection.iterator();
      if( ite.hasNext() )
        prettyFun.format( builder, ite.next() );
      while( ite.hasNext() )
        prettyFun.format( builder.append( ", " ), ite.next() );
      builder.append( " ]" );

    };

  }

  /**
   * Format a collection of object.
   *
   * @param collection the collection to format.
   * @return an object implementing {@code toString} formatting the collection.
   **/
  public static Object coll( Collection<?> collection ) {
    return lazy( coll( StringBuilder::append ), collection );
  }

  /**
   * Format a commit id.
   *
   * @param builder the builder to append.
   * @param objectId the commit id to format.
   **/
  public static void id( StringBuilder builder, AnyObjectId objectId ) {
    if( objectId == null )
      builder.append( "null" );
    else
      builder.append( objectId.abbreviate( 9 ).name() );
  }

  /**
   * Format a commit id.
   *
   * @param objectId the commit id to format.
   * @return a lazy formatter of the commit id.
   **/
  public static Object id( AnyObjectId objectId ) {
    return lazy( Pretty::id, objectId );
  }

  /**
   * Format a collection of commit id.
   *
   * @param coll the collection of commit id.
   * @return a lazy formatter of the collection.
   **/
  public static Object ids( Collection<? extends AnyObjectId> coll ) {
    return lazy( coll( Pretty::id ), coll );
  }

  /**
   * Format a commit ref.
   *
   * @param builder the builder to append.
   * @param ref the commit ref to format.
   **/
  public static void ref( StringBuilder builder, Ref ref ) {
    if( ref == null )
      builder.append( "null (null)" );
    else {
      builder.append( ref.getName() ).append( "(" );
      id( builder, ref.getObjectId() );
      builder.append( ")" );
    }
  }

  /**
   * Format a commit ref.
   *
   * @param ref the commit ref to format.
   * @return a lazy formatter of the commit ref.
   **/
  public static Object ref( Ref ref ) {
    return lazy( Pretty::ref, ref );
  }

  /**
   * Format a collection of commit ref.
   *
   * @param coll the collection to format.
   * @return a lazy formatter of the collection.
   **/
  public static Object refs( Collection<? extends Ref> coll ) {
    return lazy( coll( Pretty::ref ), coll );
  }

}
