package io.github.atos_digital_id.paprika;

import static io.github.atos_digital_id.paprika.project.ArtifactDefProvider.isPaprikaVersion;

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
import org.apache.maven.model.PluginExecution;
import org.apache.maven.model.building.DefaultModelProcessor;
import org.apache.maven.model.building.ModelProcessor;

import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.history.ArtifactStatus;
import io.github.atos_digital_id.paprika.history.ArtifactStatusExaminer;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactDefProvider;
import io.github.atos_digital_id.paprika.project.ArtifactId;
import io.github.atos_digital_id.paprika.utils.ModelWalker;
import io.github.atos_digital_id.paprika.utils.ModelWalker.GAV;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;

/**
 * Paprika model processor modifies versions, force {@link PaprikaUpdatePomMojo}
 * usage and injects properties.
 **/
@Named
@Singleton
public class PaprikaModelProcessor extends DefaultModelProcessor {

  public static final String TIMESTAMP_PROPERTY = "project.build.outputTimestamp";

  private static final String PHASE = "prepare-package";

  @Inject
  private PaprikaLogger logger;

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
    return updateModel( super.read( input, options ) );
  }

  @Override
  public Model read( Reader input, Map<String, ?> options ) throws IOException {
    return updateModel( setPom( super.read( input, options ), options ) );
  }

  @Override
  public Model read( InputStream input, Map<String, ?> options ) throws IOException {
    return updateModel( setPom( super.read( input, options ), options ) );
  }

  private Model setPom( Model model, Map<String, ?> options ) {

    Source source = (Source) options.get( ModelProcessor.SOURCE );
    if( source != null ) {
      File pom = new File( source.getLocation() );
      if( pom.isFile() )
        model.setPomFile( pom );
    }

    return model;

  }

  private Model updateModel( Model model ) throws IOException {

    if( !isPaprikaVersion( model ) )
      return model;

    ArtifactDef def = artifactDefProvider.getDef( model );

    logger.log( "Updating model of {} ({})", def, model );

    ArtifactStatus status = artifactStatusExaminer.examine( def );
    model.setVersion( status.getVersionAsString() );

    List<GAV> gavs = new ArrayList<>();
    modelWalker.visitModel( model, gavs::add );
    for( GAV gav : gavs )
      if( isPaprikaVersion( gav ) ) {
        ArtifactDef dep = artifactDefProvider.getDef( gav.id() );
        ArtifactStatus depStatus = artifactStatusExaminer.examine( dep );
        gav.setVersion( depStatus.getVersionAsString() );
      }

    Properties properties = model.getProperties();
    if( !properties.containsKey( TIMESTAMP_PROPERTY ) && configHandler.get( def ).isReproducible() )
      properties.put( TIMESTAMP_PROPERTY, status.getLastModificationAsString() );
    addProperties( properties, null, status );
    for( ArtifactDef d : artifactDefProvider.getAllDefs() )
      addProperties( properties, d.getArtifactId(), artifactStatusExaminer.examine( d ) );
    properties.put(
        ArtifactDefProvider.ANALYZED_KEY,
        ArtifactId.toString( artifactDefProvider.getAllDefs() ) );

    addUpdatePomMojo( model );

    return model;

  }

  private void addProperties( Properties properties, String id, ArtifactStatus status ) {

    String prefix = id == null ? "paprika" : "paprika." + id;
    properties.put( prefix + ".lastCommit", status.getLastCommitAsString() );
    properties.put( prefix + ".lastCommit.short", status.getLastCommitAsShortString() );
    properties.put( prefix + ".lastModification", status.getLastModificationAsString() );
    properties.put( prefix + ".tagCommit", status.getTagCommitAsString() );
    properties.put( prefix + ".tagCommit.short", status.getTagCommitAsShortString() );
    properties.put( prefix + ".refName", status.getRefName() );
    properties.put( prefix + ".baseVersion", status.getBaseVersionAsString() );
    properties.put( prefix + ".snapshot", status.getSnapshotAsString() );
    properties.put( prefix + ".pristine", status.getPristineAsString() );
    properties.put( prefix, status.getVersionAsString() );

  }

  private void addUpdatePomMojo( Model model ) {

    Build build = model.getBuild();
    if( build == null )
      model.setBuild( build = new Build() );

    List<Plugin> plugins = build.getPlugins();
    if( plugins == null )
      build.setPlugins( plugins = new ArrayList<>() );

    Plugin plugin = plugins.stream().filter( x -> {
      return paprikaBuildInfo.getGroupId().equalsIgnoreCase( x.getGroupId() )
          && paprikaBuildInfo.getArtifactId().equalsIgnoreCase( x.getArtifactId() );
    } ).findFirst().orElse( null );
    if( plugin == null ) {
      plugin = new Plugin();
      plugin.setGroupId( paprikaBuildInfo.getGroupId() );
      plugin.setArtifactId( paprikaBuildInfo.getArtifactId() );
      plugin.setVersion( paprikaBuildInfo.getVersion() );
      plugins.add( 0, plugin );
    }

    List<PluginExecution> executions = plugin.getExecutions();
    if( executions == null )
      plugin.setExecutions( executions = new ArrayList<>() );

    PluginExecution execution = executions.stream()
        .filter( x -> PHASE.equalsIgnoreCase( x.getPhase() ) ).findFirst().orElse( null );
    if( execution == null ) {
      execution = new PluginExecution();
      execution.setPhase( PHASE );
      executions.add( execution );
    }

    List<String> goals = execution.getGoals();
    if( goals == null )
      execution.setGoals( goals = new ArrayList<>() );
    if( !goals.contains( PaprikaUpdatePomMojo.GOAL ) )
      goals.add( PaprikaUpdatePomMojo.GOAL );

  }

}
