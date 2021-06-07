package io.github.atos_digital_id.paprika.project;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.maven.model.Model;

import lombok.Getter;
import lombok.NonNull;

/**
 * Module definition and properties.
 **/
public class ArtifactDef extends ArtifactId {

  private final ArtifactDefProvider artifactDefProvider;

  /**
   * Packaging of the module.
   *
   * @return the packaging.
   **/
  @Getter
  private final String packaging;

  /**
   * Parent id of the module.
   *
   * @return the parent id.
   **/
  @Getter
  private final ArtifactId parentId;

  /**
   * Parent module.
   *
   * @return the parent module.
   **/
  @Getter( lazy = true )
  private final Optional<ArtifactDef> parent = this.parentId == null ? Optional.empty()
      : Optional.of( this.artifactDefProvider.getDef( this.parentId ) );

  /**
   * Direct dependency ids.
   *
   * @return the direct dependency ids.
   **/
  private final Collection<ArtifactId> dependencyIds;

  /**
   * Direct dependencies.
   *
   * @return the direct dependencies.
   **/
  @Getter( lazy = true )
  private final SortedSet<ArtifactDef> dependencies = resolveDirectDepencies();

  private SortedSet<ArtifactDef> resolveDirectDepencies() {

    SortedSet<ArtifactDef> deps = new TreeSet<>();
    for( ArtifactId id : dependencyIds )
      deps.add( artifactDefProvider.getDef( id ) );

    return Collections.unmodifiableSortedSet( deps );

  }

  /**
   * All dependencies. Recursively computed.
   *
   * @return all the dependencies.
   **/
  @Getter( lazy = true )
  private final SortedSet<ArtifactDef> allDependencies =
      Collections.unmodifiableSortedSet( resolveAllDepencies( this, new TreeSet<>() ) );

  /**
   * Computes all the dependencies of a bunch of module.
   *
   * @param coll the modules to analyze.
   * @return all the dependencies of the modules.
   **/
  public static SortedSet<ArtifactDef> getAllDependencies(
      Collection<? extends ArtifactDef> coll ) {

    SortedSet<ArtifactDef> deps = new TreeSet<>();
    for( ArtifactDef def : coll )
      resolveAllDepencies( def, deps );

    return deps;

  }

  private static SortedSet<ArtifactDef> resolveAllDepencies(
      ArtifactDef def,
      SortedSet<ArtifactDef> deps ) {

    for( ArtifactDef dep : def.getDependencies() )
      if( deps.add( dep ) )
        resolveAllDepencies( dep, deps );

    Optional<ArtifactDef> parent = def.getParent();
    while( parent.map( deps::add ).orElse( false ) )
      parent = parent.flatMap( ArtifactDef::getParent );

    return deps;

  }

  /**
   * Path of the pom file. Relatively to the user current directory.
   *
   * @return the path of the pom file.
   **/
  private final Path relativePom;

  /**
   * Absolute path of the pom file.
   *
   * @return the path of the pom file.
   **/
  @Getter( lazy = true )
  private final Path pom = relativePom.toAbsolutePath().normalize();

  /**
   * Working directory. Directory of the pom file.
   *
   * @return the working directory.
   **/
  @Getter( lazy = true )
  private final Path workingDir = getPom().getParent();

  /**
   * Loaded model from the working dir.
   *
   * @return the loaded model.
   **/
  @Getter
  private final Model model;

  /**
   * All module names.
   *
   * @return module names.
   **/
  @Getter
  private final Set<String> modules;

  public ArtifactDef(
      @NonNull ArtifactDefProvider artifactDefProvider,
      @NonNull String groupId,
      @NonNull String artifactId,
      @NonNull String packaging,
      ArtifactId parentId,
      @NonNull Collection<ArtifactId> dependencyIds,
      @NonNull Path relativePom,
      @NonNull Model model,
      @NonNull Set<String> modules ) {

    super( groupId, artifactId );
    this.artifactDefProvider = artifactDefProvider;
    this.packaging = packaging;
    this.parentId = parentId;
    this.dependencyIds = new TreeSet<>( dependencyIds );
    this.relativePom = relativePom;
    this.model = model;
    this.modules = modules;

  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public boolean equals( Object obj ) {
    return this == obj || super.equals( obj );
  }

  @Override
  public String toString() {
    return super.toString();
  }

}
