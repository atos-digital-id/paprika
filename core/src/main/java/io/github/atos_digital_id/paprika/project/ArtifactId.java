package io.github.atos_digital_id.paprika.project;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.project.MavenProject;

import io.github.atos_digital_id.paprika.utils.Patterns;
import lombok.Data;
import lombok.NonNull;

/**
 * Identifier of an artefact, based only on the group id and the artifact id.
 **/
@Data
public class ArtifactId implements Comparable<ArtifactId> {

  /**
   * Group id.
   *
   * @return the group id.
   **/
  @NonNull
  private final String groupId;

  /**
   * Artifact id.
   *
   * @return the artifact id.
   **/
  @NonNull
  private final String artifactId;

  @Override
  public String toString() {
    return this.artifactId;
  }

  /**
   * Extract an id from a Maven project.
   *
   * @param project the project.
   * @return the id of the project.
   **/
  public static ArtifactId from( @NonNull MavenProject project ) {
    return new ArtifactId( project.getGroupId(), project.getArtifactId() );
  }

  /**
   * Extract an id from a Maven model.
   *
   * @param model the model.
   * @return the id of the model.
   **/
  public static ArtifactId from( @NonNull Model model ) {

    String groupId = model.getGroupId();
    if( groupId == null && model.getParent() != null )
      groupId = model.getParent().getGroupId();

    return new ArtifactId( groupId, model.getArtifactId() );

  }

  /**
   * Extract an id from a parent of a model.
   *
   * @param parent the parent.
   * @return the id of the parent.
   **/
  public static ArtifactId from( @NonNull Parent parent ) {
    return new ArtifactId( parent.getGroupId(), parent.getArtifactId() );
  }

  @Override
  public int compareTo( @NonNull ArtifactId other ) {
    return ArtifactId.compare( this, other );
  }

  /**
   * Compares two identifiers lexigraphically. First the group id, then the
   * artifact id.
   *
   * @param a an id.
   * @param b another id.
   * @return {@code 0} if {@code a.equals(b)}, {@code -1} if {@code a} is before
   *         {@code b}, {@code 1} otherwise.
   **/
  public static int compare( @NonNull ArtifactId a, @NonNull ArtifactId b ) {

    int res = a.getGroupId().compareTo( b.getGroupId() );
    if( res == 0 )
      res = a.getArtifactId().compareTo( b.getArtifactId() );

    return res;

  }

  /**
   * Format a collection of ids.
   *
   * @param ids the collection to format.
   * @return the formatted collection.
   * @see ArtifactId#fromString
   **/
  public static String toString( @NonNull Collection<? extends ArtifactId> ids ) {

    StringBuilder builder = new StringBuilder();

    Iterator<? extends ArtifactId> ite = ids.iterator();
    if( ite.hasNext() ) {
      ArtifactId id = ite.next();
      builder.append( id.getGroupId() ).append( ":" ).append( id.getArtifactId() );
    }
    while( ite.hasNext() ) {
      ArtifactId id = ite.next();
      builder.append( "," ).append( id.getGroupId() ).append( ":" ).append( id.getArtifactId() );
    }

    return builder.toString();

  }

  /**
   * Extract a collection of ids from a String.
   *
   * @param value the String to parse.
   * @return the contained ids.
   * @see ArtifactId#toString(Collection)
   **/
  public static SortedSet<ArtifactId> fromString( @NonNull String value ) {

    SortedSet<ArtifactId> ids = new TreeSet<>();
    if( value.isEmpty() )
      return ids;

    for( String elt : Patterns.split( value, ',' ) ) {

      if( elt.isEmpty() )
        continue;

      List<String> parts = Patterns.split( elt, ':' );
      if( parts.size() != 2 )
        throw new IllegalArgumentException( "Can not read artifact ids from '" + value + "'." );

      ids.add( new ArtifactId( parts.get( 0 ).trim(), parts.get( 1 ).trim() ) );

    }

    return ids;

  }

}
