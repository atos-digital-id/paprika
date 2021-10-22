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
import org.apache.maven.model.Model;
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
 * Paprika model processor modifies versions and injects properties.
 **/
@Named
@Singleton
public class PaprikaModelProcessor extends DefaultModelProcessor {

  public static final String TIMESTAMP_PROPERTY = "project.build.outputTimestamp";

  @Inject
  private PaprikaLogger logger;

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

    if( !isPaprikaVersion( model ) )
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

}
