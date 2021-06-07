package io.github.atos_digital_id.paprika.project;

import static io.github.atos_digital_id.paprika.utils.ModelWalker.GAVUsage.MODEL;
import static io.github.atos_digital_id.paprika.utils.ModelWalker.GAVUsage.PARENT;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Profile;
import org.apache.maven.model.io.ModelReader;

import io.github.atos_digital_id.paprika.PaprikaBuildInfo;
import io.github.atos_digital_id.paprika.utils.ModelWalker;
import io.github.atos_digital_id.paprika.utils.ModelWalker.GAV;
import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import lombok.Getter;
import lombok.NonNull;

/**
 * Container of {@link ArtifactDef}.
 **/
@Named
@Singleton
public class ArtifactDefProvider {

  /**
   * Paprika version placeholder.
   **/
  public static final String VERSION_PLACEHOLDER = "${paprika}";

  /**
   * Test if the model is defined with the Paprika version placeholder.
   *
   * @param model the model to test.
   * @return true if the model is flagged with the Paprika version placeholder.
   **/
  public static boolean isPaprikaVersion( @NonNull Model model ) {

    String version = model.getVersion();
    if( version == null ) {
      Parent parent = model.getParent();
      if( parent != null )
        version = parent.getVersion();
    }

    return VERSION_PLACEHOLDER.equals( version );

  }

  /**
   * Test if the parent is defined with the Paprika version placeholder.
   *
   * @param parent the parent to test.
   * @return true if the parent is flagged with the Paprika version placeholder.
   **/
  public static boolean isPaprikaVersion( @NonNull Parent parent ) {
    return VERSION_PLACEHOLDER.equals( parent.getVersion() );
  }

  /**
   * Test if the GAV is defined with the Paprika version placeholder.
   *
   * @param gav the GAV to test.
   * @return true if the GAV is flagged with the Paprika version placeholder.
   **/
  public static boolean isPaprikaVersion( @NonNull GAV gav ) {
    return VERSION_PLACEHOLDER.equals( gav.getVersion() );
  }

  /**
   * Maven property containing all module defined with the Paprika version
   * placeholder.
   **/
  public static final String ANALYZED_KEY = "paprika.analyzed";

  @Inject
  private PaprikaLogger logger;

  @Inject
  private PaprikaBuildInfo paprikaBuildInfo;

  @Inject
  private ModelReader modelReader;

  @Inject
  private ModelWalker modelWalker;

  private final ArtifactIdCache<ArtifactDef> cache = new HashMapArtifactIdCache<>();

  private final Set<ArtifactDef> defs = new HashSet<>();

  @Getter( lazy = true )
  private final SortedSet<ArtifactDef> allDefs =
      Collections.unmodifiableSortedSet( new TreeSet<>( defs ) );

  /**
   * Returns the {@link ArtifactDef} associated with a {@link ArtifactId}.
   *
   * @param id the ArtifactId to search.
   * @return the associated ArtifactDef.
   **/
  public ArtifactDef getDef( @NonNull ArtifactId id ) {
    return cache.get( id, () -> {
      throw new IllegalStateException( "The artifact " + id + " has not been loaded" );
    } );
  }

  /**
   * Returns the {@link ArtifactDef} from a Maven model. All related module
   * (recursively parent, dependencies, sub-modules) are loaded and cached.
   *
   * @param model the model to analyze.
   * @return the associated ArtifactDef.
   * @throws IOException an IO exception with the filesystem has occured.
   **/
  public ArtifactDef getDef( @NonNull Model model ) throws IOException {
    ArtifactId id = ArtifactId.from( model );
    return cache.get( id, () -> loadDef( model, new HashSet<>() ) );
  }

  private ArtifactDef loadDef( Model model, Set<ArtifactId> force ) {

    ArtifactId id = ArtifactId.from( model );

    Optional<ArtifactDef> optionalDef = cache.peek( id );
    if( optionalDef.isPresent() )
      return optionalDef.get();

    force.addAll( ArtifactId.fromString( model.getProperties().getProperty( ANALYZED_KEY, "" ) ) );

    // get all modules
    Set<String> modules = new HashSet<>();

    List<String> listModules = model.getModules();
    if( listModules != null )
      modules.addAll( listModules );

    List<Profile> profiles = model.getProfiles();
    if( profiles != null )
      for( Profile profile : profiles ) {
        listModules = profile.getModules();
        if( listModules != null )
          modules.addAll( listModules );
      }

    // Artifact definition
    ArtifactDef def = null;

    if( force.contains( id ) || isPaprikaVersion( model ) ) {

      logger.reset( "Loading of {}: ", id );
      try {

        String packaging = model.getPackaging();
        logger.log( "packaging: {}", packaging );

        ArtifactId parentId = null;
        Parent parent = model.getParent();
        if( parent != null ) {
          ArtifactId tmpParentId = ArtifactId.from( parent );
          if( force.contains( tmpParentId ) || isPaprikaVersion( parent ) )
            parentId = tmpParentId;
        }
        logger.log( "parent: {}", parentId );

        Set<ArtifactId> dependencies = new HashSet<>();
        modelWalker.visitModel( model, gav -> {

          // exclude model itself and parent
          if( gav.getUsage() == MODEL || gav.getUsage() == PARENT )
            return;

          // ugly but the 'recursive' build of paprika need it
          if( id.getGroupId().equals( paprikaBuildInfo.getGroupId() )
              && gav.getGroupId().equals( paprikaBuildInfo.getGroupId() )
              && gav.getArtifactId().equals( paprikaBuildInfo.getArtifactId() ) )
            return;

          ArtifactId gavId = gav.id();
          if( force.contains( gavId ) || isPaprikaVersion( gav ) )
            dependencies.add( gavId );

        } );
        logger.log( "dependencies: {}", Pretty.coll( dependencies ) );

        Path pom = model.getPomFile().toPath();
        logger.log( "pom path: {}", pom );

        def = new ArtifactDef(
            this,
            id.getGroupId(),
            id.getArtifactId(),
            packaging,
            parentId,
            dependencies,
            pom,
            model,
            modules );
        cache.set( id, def );
        defs.add( def );

      } finally {
        logger.restore();
      }

    }

    Path baseDir = model.getProjectDirectory().getAbsoluteFile().toPath();

    // parent
    loadParent( id, model, baseDir, force );

    // modules
    for( String module : modules )
      loadModule( id, model, baseDir, module, force );

    return def;

  }

  private void loadParent( ArtifactId id, Model model, Path baseDir, Set<ArtifactId> force ) {

    Path pom;

    logger.reset( "Looking for parent pom of {}: ", id );
    try {

      Parent parent = model.getParent();
      if( parent == null ) {
        logger.log( "No parent found" );
        return;
      }

      ArtifactId parentId = ArtifactId.from( parent );
      if( !force.contains( parentId ) && !isPaprikaVersion( parent ) ) {
        logger.log( "Out of scope" );
        return;
      }

      Optional<ArtifactDef> optionalDef = cache.peek( parentId );
      if( optionalDef.isPresent() ) {
        logger.log( "Parent already loaded" );
        return;
      }

      String relativePom = parent.getRelativePath();
      if( relativePom == null || relativePom.isEmpty() ) {
        logger.log( "Pom path empty" );
        return;
      }

      pom = baseDir.resolve( relativePom );
      logger.log( "Looking pom at {}", pom );
      if( !Files.exists( pom ) || !Files.isRegularFile( pom ) )
        throw new IllegalStateException(
            "The pom of " + parentId + ", parent of " + id + ", doesn't exists (" + pom + ")." );

    } finally {
      logger.restore();
    }

    loadDef( pom, force );

  }

  private void loadModule(
      ArtifactId id,
      Model model,
      Path baseDir,
      String module,
      Set<ArtifactId> force ) {

    Path pom = baseDir.resolve( module ).resolve( "pom.xml" );
    logger.log( "Looking for module {} of {}: Looking pom at {}", module, id, pom );
    if( !Files.exists( pom ) || !Files.isRegularFile( pom ) )
      throw new IllegalStateException(
          "The pom of " + module + ", module of " + model + ", doesn't exists (" + pom + ")." );

    loadDef( pom, force );

  }

  private void loadDef( Path pom, Set<ArtifactId> force ) {

    try {
      loadDef( modelReader.read( pom.toFile(), null ), force );
    } catch( IOException ex ) {
      throw new IllegalStateException( "Can not load pom " + pom + ": " + ex.getMessage(), ex );
    }

  }

}
