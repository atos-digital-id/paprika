package io.github.atos_digital_id.paprika;

import static io.github.atos_digital_id.paprika.project.ArtifactDefProvider.isPaprikaVersion;
import static io.github.atos_digital_id.paprika.utils.templating.value.CommitValue.getNameOf;
import static io.github.atos_digital_id.paprika.utils.templating.value.CommitValue.getPrettyNameOf;
import static io.github.atos_digital_id.paprika.utils.templating.value.CommitValue.getStringDateOf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.building.Source;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelProcessor;
import org.eclipse.jgit.revwalk.RevCommit;

import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.history.ArtifactStatus;
import io.github.atos_digital_id.paprika.history.ArtifactStatusExaminer;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactDefProvider;
import io.github.atos_digital_id.paprika.project.ArtifactId;
import io.github.atos_digital_id.paprika.utils.ModelWalker;
import io.github.atos_digital_id.paprika.utils.ModelWalker.GAV;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import io.github.atos_digital_id.paprika.version.Version;

/**
 * Paprika model processor modifies versions and injects properties.
 **/
@Named
@Singleton
public class PaprikaModelProcessor extends DefaultModelProcessor {

  /**
   * {@code project.build.outputTimestamp}
   **/
  public static final String TIMESTAMP_PROPERTY = "project.build.outputTimestamp";

  @Inject
  private PaprikaLogger logger;

  @Inject
  private GitHandler git;

  @Inject
  private PaprikaBuildInfo paprikaBuildInfo;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private ArtifactDefProvider artifactDefProvider;

  @Inject
  private ArtifactStatusExaminer artifactStatusExaminer;

  @Inject
  private ModelWalker modelWalker;

  @Override
  public Model read( File input, Map<String, ?> options ) throws IOException {
    return updateModel( super.read( input, options ), options );
  }

  @Override
  public Model read( Reader input, Map<String, ?> options ) throws IOException {
    return updateModel( super.read( input, options ), options );
  }

  @Override
  public Model read( InputStream input, Map<String, ?> options ) throws IOException {
    return updateModel( super.read( input, options ), options );
  }

  private Model updateModel( Model model, Map<String, ?> options ) throws IOException {

    if( Config.isSkipped() || !isPaprikaVersion( model ) )
      return model;

    Source source = (Source) options.get( ModelProcessor.SOURCE );
    if( source != null ) {
      File pom = new File( source.getLocation() );
      if( pom.isFile() )
        model.setPomFile( pom );
    }

    ArtifactDef def = artifactDefProvider.getDef( model );

    logger.log( "Updating model of {} ({})", def, model );

    ArtifactStatus status = artifactStatusExaminer.examine( def );
    model.setVersion( status.getVersion().toString() );

    List<GAV> gavs = new ArrayList<>();
    modelWalker.visitModel( model, gavs::add );
    for( GAV gav : gavs )
      if( isPaprikaVersion( gav ) ) {
        ArtifactDef dep = artifactDefProvider.getDef( gav.id() );
        ArtifactStatus depStatus = artifactStatusExaminer.examine( dep );
        gav.setVersion( depStatus.getVersion().toString() );
      }

    Properties properties = model.getProperties();
    if( !properties.containsKey( TIMESTAMP_PROPERTY ) && configHandler.get( def ).isReproducible() )
      properties.put( TIMESTAMP_PROPERTY, getStringDateOf( git, status.getLastCommit() ) );
    addProperties( properties, null, status );
    for( ArtifactDef d : artifactDefProvider.getAllDefs() )
      addProperties( properties, d.getArtifactId(), artifactStatusExaminer.examine( d ) );
    properties.put(
        ArtifactDefProvider.ANALYZED_KEY,
        ArtifactId.toString( artifactDefProvider.getAllDefs() ) );

    addPaprikaDependency( model );

    return model;

  }

  private void addProperties( Properties properties, String id, ArtifactStatus status ) {

    String prefix = id == null ? "paprika" : "paprika." + id;

    RevCommit lastCommit = status.getLastCommit();
    properties.put( prefix + ".lastCommit", getNameOf( lastCommit ) );
    properties.put( prefix + ".lastCommit.short", getPrettyNameOf( lastCommit ) );
    properties.put( prefix + ".lastModification", getStringDateOf( git, lastCommit ) );

    RevCommit tagCommit = status.getTagCommit();
    properties.put( prefix + ".tagCommit", getNameOf( tagCommit ) );
    properties.put( prefix + ".tagCommit.short", getPrettyNameOf( tagCommit ) );

    String refName = status.getRefName();
    properties.put( prefix + ".refName", refName == null ? "" : refName );

    Version baseVersion = status.getBaseVersion();
    properties.put( prefix + ".baseVersion", baseVersion == null ? "" : baseVersion.toString() );

    Version version = status.getVersion();
    properties.put( prefix + ".snapshot", Boolean.toString( version.isSnapshot() ) );
    properties.put( prefix + ".pristine", Boolean.toString( !version.isSnapshot() ) );
    properties.put( prefix, version.toString() );

  }

  private void addPaprikaDependency( Model model ) {

    String g = paprikaBuildInfo.getGroupId();
    String a = paprikaBuildInfo.getArtifactId();
    String v = paprikaBuildInfo.getVersion();

    Build build = model.getBuild();
    if( build == null )
      model.setBuild( build = new Build() );

    List<Plugin> plugins = build.getPlugins();
    if( plugins == null )
      build.setPlugins( plugins = new ArrayList<>() );

    Plugin plugin = plugins.stream().filter( x -> {
      return g.equalsIgnoreCase( x.getGroupId() ) && a.equalsIgnoreCase( x.getArtifactId() );
    } ).findFirst().orElse( null );
    if( plugin == null ) {
      plugin = new Plugin();
      plugin.setGroupId( g );
      plugin.setArtifactId( a );
      plugin.setVersion( v );
      plugins.add( 0, plugin );
    }

  }

}
