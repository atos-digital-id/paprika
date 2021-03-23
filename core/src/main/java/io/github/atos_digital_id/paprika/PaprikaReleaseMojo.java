package io.github.atos_digital_id.paprika;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.inject.Inject;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;

import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.history.ArtifactStatus;
import io.github.atos_digital_id.paprika.history.ArtifactStatusExaminer;
import io.github.atos_digital_id.paprika.history.ArtifactStatusExaminer.IncrementPart;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactDefProvider;
import io.github.atos_digital_id.paprika.project.ArtifactTags;
import io.github.atos_digital_id.paprika.version.Version;

/**
 * Paprika release Mojo. Can be helpful to releasing modules: it displays or
 * execute the Git commands for tagging the currently modified modules.
 **/
@Mojo( name = PaprikaReleaseMojo.GOAL, requiresDirectInvocation = true, aggregator = true )
public class PaprikaReleaseMojo extends AbstractMojo {

  public static final String GOAL = "release";

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private ArtifactDefProvider artifactDefProvider;

  @Inject
  private ArtifactStatusExaminer artifactStatusExaminer;

  @Inject
  private ArtifactTags artifactTags;

  /**
   * Seek for sub-module to also release.
   **/
  @Parameter( property = "releaseModules", defaultValue = "true" )
  private boolean releaseModules;

  /**
   * Part of version to increment. Can be {@code MAJOR}, {@code MINOR} or
   * {@code PATCH}.
   **/
  @Parameter( property = "increment", defaultValue = "MINOR" )
  private String incrementPart;

  /**
   * Write the proposed commands in a file.
   **/
  @Parameter( property = "outputFile" )
  private File outputFile;

  /**
   * Execute proposed commands.
   **/
  @Parameter( property = "exec", defaultValue = "false" )
  private boolean exec;

  @Parameter( defaultValue = "${session}", readonly = true, required = true )
  private MavenSession session;

  @Parameter( defaultValue = "${project}", readonly = true, required = true )
  private MavenProject project;

  @Override
  public synchronized void execute() throws MojoExecutionException {

    try {

      IncrementPart part = Enum.valueOf( IncrementPart.class, incrementPart.trim().toUpperCase() );
      artifactStatusExaminer.setIncrementPart( part );
      gitHandler.afterSessionStart( session );

      ArtifactDef artifactDef = artifactDefProvider.getDef( project.getModel() );

      SortedSet<ArtifactDef> scope;
      if( releaseModules ) {

        scope = searchModules( artifactDef );

        Iterator<ArtifactDef> ite = scope.iterator();
        while( ite.hasNext() ) {
          ArtifactDef def = ite.next();
          if( configHandler.get( def ).isReleaseIgnored() )
            ite.remove();
        }

        scope.addAll( ArtifactDef.getAllDependencies( scope ) );

      } else {

        scope = new TreeSet<>();
        scope.add( artifactDef );
        scope.addAll( artifactDef.getAllDependencies() );

      }

      List<ReleaseCommand> commands = new LinkedList<>();

      for( ArtifactDef def : scope ) {

        ArtifactStatus status = artifactStatusExaminer.examine( def );
        if( !status.isSnapshot() )
          continue;

        Version current = status.getVersion();
        Version version = new Version(
            current.getMajor(),
            current.getMinor(),
            current.getPatch(),
            Version.EMPTY_STRINGS,
            Version.EMPTY_STRINGS );

        if( ObjectId.zeroId().equals( status.getLastCommit() ) )
          throw new MojoExecutionException(
              "The project " + def + " has modifications not yet committed." );

        commands.add( new ReleaseCommand( def, status, version ) );

      }

      if( outputFile != null ) {
        Path path = outputFile.toPath();
        Files.createDirectories( path.getParent() );
        try( Writer writer = Files.newBufferedWriter( path ) ) {
          for( ReleaseCommand command : commands ) {
            writer.write( command.toString() );
            writer.write( System.lineSeparator() );
          }
        }
      }

      if( !commands.isEmpty() ) {

        getLog().info( "" );

        if( exec )
          getLog().info( "Running commands:" );
        else
          getLog().info( "Git commands:" );

        for( ReleaseCommand command : commands ) {
          getLog().info( "  " + command );
          if( exec )
            command.exec();
        }

        getLog().info( "" );

      } else {

        getLog().info( "" );
        getLog().info( "Nothing to release" );
        getLog().info( "" );

      }

    } catch( MavenExecutionException ex ) {
      throw new MojoExecutionException( "Maven exception: " + ex.getMessage(), ex );
    } catch( IOException ex ) {
      throw new MojoExecutionException( "IO exception: " + ex.getMessage(), ex );
    }

  }

  private SortedSet<ArtifactDef> searchModules( ArtifactDef artifactDef ) {

    Set<ArtifactDef> remainings = new HashSet<>( artifactDefProvider.getAllDefs() );
    remainings.remove( artifactDef );

    SortedSet<ArtifactDef> modules = new TreeSet<>();
    modules.add( artifactDef );

    boolean found = true;
    while( found ) {

      found = false;

      for( ArtifactDef def : new ArrayList<>( remainings ) ) {
        if( def.getParent().map( modules::contains ).orElse( false ) ) {
          remainings.remove( def );
          modules.add( def );
          found = true;
        }
      }

    }

    return modules;

  }

  private class ReleaseCommand {

    private final ArtifactStatus status;

    private final Config config;

    private final String message;

    private final String tag;

    private final String command;

    public ReleaseCommand( ArtifactDef def, ArtifactStatus status, Version version ) {

      this.status = status;

      this.config = configHandler.get( def );

      this.tag = artifactTags.getShortTag( def, version );
      this.message = getMessage( def, status, version );

      this.command = buildCommand();

    }

    public String buildCommand() {

      StringBuilder builder = new StringBuilder();

      builder.append( "git tag" );

      if( config.isAnnotated() ) {

        if( config.isSigned() )
          builder.append( " -s" );
        else
          builder.append( " -a" );

        builder.append( " -m \"" ).append( message ).append( "\"" );

      }

      builder.append( " \"" ).append( tag ).append( "\"" );

      if( config.isLastModification() )
        builder.append( " " ).append( status.getLastCommitAsShortString() );

      return builder.toString();

    }

    @Override
    public String toString() {
      return this.command;
    }

    public void exec() throws IOException {

      try {

        TagCommand tagCommand = gitHandler.git().tag();

        if( !config.isAnnotated() ) {

          tagCommand.setAnnotated( false );

        } else {

          tagCommand.setAnnotated( true );
          tagCommand.setSigned( config.isSigned() );
          tagCommand.setMessage( message );

        }

        tagCommand.setName( tag );

        if( config.isLastModification() )
          tagCommand.setObjectId( gitHandler.repository().parseCommit( status.getLastCommit() ) );

        tagCommand.call();

      } catch( GitAPIException ex ) {
        throw new IOException( "Git API exception: " + ex.getMessage(), ex );
      }

    }

  }

  private String getMessage( ArtifactDef def, ArtifactStatus status, Version version ) {

    Map<String, String> props = new HashMap<>();
    props.put( "groupId", def.getGroupId() );
    props.put( "artifactId", def.getArtifactId() );
    props.put( "packaging", def.getPackaging() );
    def.getParent().ifPresent( parent -> {
      props.put( "parent.groupId", parent.getGroupId() );
      props.put( "parent.artifactId", parent.getArtifactId() );
      props.put( "parent.packaging", parent.getArtifactId() );
    } );
    props.put( "version", version.toString() );
    props.put( "lastCommit", status.getLastCommitAsString() );
    props.put( "lastCommit.short", status.getLastCommitAsShortString() );
    props.put( "lastModification", status.getLastModificationAsString() );
    props.put( "previousVersion", status.getBaseVersionAsString() );

    String msg = configHandler.get( def ).getReleaseMessage();
    for( Map.Entry<String, String> entry : props.entrySet() )
      msg = msg.replace( "${" + entry.getKey() + "}", entry.getValue() );

    return msg;

  }

}
