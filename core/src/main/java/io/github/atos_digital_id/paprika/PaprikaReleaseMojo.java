package io.github.atos_digital_id.paprika;

import static io.github.atos_digital_id.paprika.utils.templating.value.CommitValue.getPrettyNameOf;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.history.ArtifactStatus;
import io.github.atos_digital_id.paprika.history.ArtifactStatusExaminer;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactDefProvider;
import io.github.atos_digital_id.paprika.project.ArtifactTags;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateEngine;
import io.github.atos_digital_id.paprika.utils.templating.value.CommitValue;
import io.github.atos_digital_id.paprika.utils.templating.value.VersionValue;
import io.github.atos_digital_id.paprika.version.Version;

/**
 * Paprika release Mojo. Can be helpful to releasing modules: it displays or
 * executes the Git commands for tagging the currently modified modules. This
 * mojo can be configured with system properties:
 * <ul>
 * <li>{@code lastModification}
 * <li>{@code annotated}
 * <li>{@code signed}
 * <li>{@code message}
 * <li>{@code subModules}
 * <li>{@code increment}
 * <li>{@code skipTagged}
 * <li>{@code output}
 * <li>{@code exec}
 * </ul>
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

  @Parameter( defaultValue = "${session}", readonly = true, required = true )
  private MavenSession session;

  @Parameter( defaultValue = "${project}", readonly = true, required = true )
  private MavenProject project;

  @Override
  public synchronized void execute() throws MojoExecutionException {

    try {

      gitHandler.afterSessionStart( session );

      ArtifactDef artifactDef = artifactDefProvider.getDef( project.getModel() );

      SortedSet<ArtifactDef> scope = searchModules( artifactDef );
      scope.addAll( ArtifactDef.getAllDependencies( scope ) );

      List<ReleaseCommand> commands = new LinkedList<>();

      try( RevWalk revwalk = new RevWalk( gitHandler.repository() ) ) {

        for( ArtifactDef def : scope ) {

          ArtifactStatus status = artifactStatusExaminer.examine( def );
          if( !status.getVersion().isSnapshot() )
            continue;

          RevCommit lastCommit = status.getLastCommit();

          if( lastCommit == null )
            throw new MojoExecutionException(
                "The project " + def + " has modifications not yet committed." );

          if( configHandler.get( def ).isSkipTagged()
              && lastCommitIsTagged( def, status, revwalk ) )
            continue;

          Version current = status.getVersion();
          Version version = new Version(
              current.getMajor(),
              current.getMinor(),
              current.getPatch(),
              Version.EMPTY_STRINGS,
              Version.EMPTY_STRINGS );

          commands.add( new ReleaseCommand( def, status, version ) );

        }

      }

      Path output = configHandler.get().getReleaseOutput();
      if( output != null ) {
        Files.createDirectories( output.getParent() );
        try( Writer writer = Files.newBufferedWriter( output ) ) {
          for( ReleaseCommand command : commands ) {
            writer.write( command.toString() );
            writer.write( System.lineSeparator() );
          }
        }
      }

      if( !commands.isEmpty() ) {

        boolean exec = configHandler.get().isReleaseExec();

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

    SortedSet<ArtifactDef> scope = new TreeSet<>();
    scope.add( artifactDef );

    Set<ArtifactDef> remainings = new HashSet<>( artifactDefProvider.getAllDefs() );
    remainings.remove( artifactDef );

    boolean found = true;
    while( found ) {

      found = false;

      for( ArtifactDef def : new ArrayList<>( remainings ) ) {

        if( configHandler.get( def ).isReleaseIgnored() ) {
          remainings.remove( def );
          continue;
        }

        ArtifactDef parent = def.getParent().orElse( null );
        if( parent == null || !configHandler.get( parent ).isReleaseSubModules() ) {
          remainings.remove( def );
          continue;
        }

        if( scope.contains( parent ) ) {
          remainings.remove( def );
          scope.add( def );
          found = true;
        }

      }

    }

    return scope;

  }

  private boolean lastCommitIsTagged( ArtifactDef def, ArtifactStatus status, RevWalk revwalk )
      throws IOException {

    for( Ref ref : artifactTags.getTags( def ) )
      if( status.getLastCommit().equals( revwalk.parseCommit( ref.getObjectId() ) ) )
        return true;

    return false;

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
      this.message = getMessage( def, status, config, version );

      this.command = buildCommand();

    }

    public String buildCommand() {

      StringBuilder builder = new StringBuilder();

      builder.append( "git tag" );

      if( config.isReleaseAnnotated() ) {

        if( config.isReleaseSigned() )
          builder.append( " -s" );
        else
          builder.append( " -a" );

        builder.append( " -m \"" ).append( message ).append( "\"" );

      }

      builder.append( " \"" ).append( tag ).append( "\"" );

      if( config.isReleaseLastModification() )
        builder.append( " " ).append( getPrettyNameOf( status.getLastCommit() ) );

      return builder.toString();

    }

    @Override
    public String toString() {
      return this.command;
    }

    public void exec() throws IOException {

      try {

        TagCommand tagCommand = gitHandler.git().tag();

        if( !config.isReleaseAnnotated() ) {

          tagCommand.setAnnotated( false );

        } else {

          tagCommand.setAnnotated( true );
          tagCommand.setSigned( config.isReleaseSigned() );
          tagCommand.setMessage( message );

        }

        tagCommand.setName( tag );

        if( config.isReleaseLastModification() )
          tagCommand.setObjectId( gitHandler.repository().parseCommit( status.getLastCommit() ) );

        tagCommand.call();

      } catch( GitAPIException ex ) {
        throw new IOException( "Git API exception: " + ex.getMessage(), ex );
      }

    }

  }

  private String getMessage(
      ArtifactDef def,
      ArtifactStatus status,
      Config config,
      Version version ) {

    String msg = config.getReleaseMessage();

    Map<String, Object> context = new HashMap<>();

    context.put( "groupId", def.getGroupId() );
    context.put( "artifactId", def.getArtifactId() );
    context.put( "packaging", def.getPackaging() );

    def.getParent().ifPresentOrElse( parent -> {
      context.put( "hasParent", true );
      context.put( "parentGroupId", parent.getGroupId() );
      context.put( "parentArtifactId", parent.getArtifactId() );
      context.put( "parentPackaging", parent.getPackaging() );
    }, () -> {
      context.put( "hasParent", false );
      context.put( "parentGroupId", "" );
      context.put( "parentArtifactId", "" );
      context.put( "parentPackaging", "" );
    } );

    context.put( "version", VersionValue.wrap( version ) );
    context.put( "baseVersion", VersionValue.wrap( status.getBaseVersion() ) );

    context.put( "lastCommit", CommitValue.wrap( gitHandler, status.getLastCommit() ) );

    return TemplateEngine.execute( config, msg, context );

  }

}
