package io.github.atos_digital_id.paprika;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevWalk;

import io.github.atos_digital_id.paprika.config.Config;
import io.github.atos_digital_id.paprika.config.ConfigHandler;
import io.github.atos_digital_id.paprika.history.ArtifactCheckers;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.project.ArtifactDefProvider;
import io.github.atos_digital_id.paprika.project.ArtifactTags;
import io.github.atos_digital_id.paprika.utils.templating.engine.TemplateEngine;
import io.github.atos_digital_id.paprika.utils.templating.value.ReleaseValue;
import io.github.atos_digital_id.paprika.version.Version;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Mojo( name = PaprikaChangelogMojo.GOAL, requiresDirectInvocation = true, aggregator = true )
public class PaprikaChangelogMojo extends AbstractMojo {

  public static final String GOAL = "changelog";

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ConfigHandler configHandler;

  @Inject
  private ArtifactDefProvider artifactDefProvider;

  @Inject
  private ArtifactCheckers artifactCheckers;

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

      ArtifactDef def = artifactDefProvider.getDef( project.getModel() );
      SortedSet<ArtifactDef> scope = new TreeSet<>();
      scope.add( def );
      scope.addAll( def.getAllDependencies() );

      List<ArtifactCheckers.Checker> checkers = new ArrayList<>();
      for( ArtifactDef d : scope )
        checkers.add( artifactCheckers.create( d ) );
      Repository repository = gitHandler.repository();

      Config config = configHandler.get();

      try( RevWalk revWalk = new RevWalk( repository ) ) {

        Map<RevCommit, SortedSet<Ref>> tagsMap = new HashMap<>();
        for( Ref ref : artifactTags.getTags( def ) ) {
          tagsMap.computeIfAbsent(
              revWalk.parseCommit( ref.getObjectId() ),
              c -> new TreeSet<Ref>(
                  Comparator.comparing( ( Ref r ) -> artifactTags.getVersion( def, r ) )
                      .reversed() ) )
              .add( ref );
        }

        HistoryFrom from = getHistoryFrom( def, revWalk );

        Predicate<RevCommit> stopHistory;
        if( from.isRoot() ) {

          stopHistory = commit -> false;

        } else {

          RevCommit fromCommit = from.getCommit();

          if( fromCommit != null ) {

            stopHistory = commit -> commit.equals( fromCommit );

          } else {

            stopHistory = new Predicate<>() {

              private boolean firstTagPassed = false;

              @Override
              public boolean test( RevCommit commit ) {
                if( tagsMap.containsKey( commit ) )
                  if( firstTagPassed )
                    return true;
                  else
                    firstTagPassed = true;
                return false;
              }

            };

          }

        }

        List<ReleaseValue> releases = new ArrayList<>();

        ReleaseStatus currentRelease = new ReleaseStatus( def );

        revWalk.setFirstParent( true );
        revWalk.markStart( getHistoryTo( def, revWalk ) );

        RevCommit current = revWalk.next();
        while( current != null && !stopHistory.test( current ) ) {

          if( tagsMap.containsKey( current ) ) {
            for( Ref ref : tagsMap.get( current ) ) {
              ReleaseValue release = currentRelease.create();
              if( release.isReleased() || release.getChanges().size() != 0 )
                releases.add( release );
              currentRelease.startNew( revWalk, ref );
            }
          }

          for( ArtifactCheckers.Checker checker : checkers )
            if( checker.isModifiedAt( revWalk, current ) ) {
              currentRelease.add( current );
              break;
            }

          current = revWalk.next();

        }

        releases.add( currentRelease.create() );

        Map<String, Object> context = new HashMap<>();
        context.put( "releases", releases );

        try( Writer writer = getOutputWriter( config ) ) {
          writer.append( TemplateEngine.execute( config, config.getChangelogTemplate(), context ) );
        }

      }

    } catch( MavenExecutionException ex ) {
      throw new MojoExecutionException( "Maven exception: " + ex.getMessage(), ex );
    } catch( IOException ex ) {
      throw new MojoExecutionException( "IO exception: " + ex.getMessage(), ex );
    }

  }

  @Data
  private static class HistoryFrom {

    private final RevCommit commit;

    private final boolean root;

  }

  private HistoryFrom getHistoryFrom( ArtifactDef def, RevWalk revWalk ) throws IOException {

    String from = configHandler.get().getChangelogFrom();
    if( from.isBlank() )
      return new HistoryFrom( null, false );

    Repository repository = gitHandler.repository();

    ObjectId id = repository.resolve( from );
    if( id != null )
      return new HistoryFrom( revWalk.parseCommit( id ), false );

    id = repository.resolve( artifactTags.getCompleteTag( def, from ) );
    if( id != null )
      return new HistoryFrom( revWalk.parseCommit( id ), false );

    if( "root".equalsIgnoreCase( from ) )
      return new HistoryFrom( null, true );

    throw new IllegalArgumentException( "\"From\" commit '" + from + "' can't be resolved." );

  }

  private RevCommit getHistoryTo( ArtifactDef def, RevWalk revWalk ) throws IOException {

    String to = configHandler.get().getChangelogTo();

    Repository repository = gitHandler.repository();

    ObjectId id = repository.resolve( to );
    if( id != null )
      return revWalk.parseCommit( id );

    id = repository.resolve( artifactTags.getCompleteTag( def, to ) );
    if( id != null )
      return revWalk.parseCommit( id );

    throw new IllegalArgumentException( "\"To\" commit '" + to + "' can't be resolved." );

  }

  @RequiredArgsConstructor
  private class ReleaseStatus {

    private final ArtifactDef def;

    private String tagName = null;

    private Version version = null;

    private RevObject taggedObj = null;

    private List<RevCommit> commits = new ArrayList<>();

    public void add( RevCommit commit ) {
      commits.add( commit );
    }

    public ReleaseValue create() {
      Collections.reverse( commits );
      return ReleaseValue.wrap( gitHandler, tagName, version, taggedObj, commits );
    }

    public void startNew( RevWalk revWalk, Ref ref ) throws IOException {
      tagName = ref.getName();
      version = artifactTags.getVersion( def, ref );
      taggedObj = revWalk.parseAny( ref.getObjectId() );
      commits = new ArrayList<>();
    }

  }

  private Writer getOutputWriter( Config config ) throws IOException {

    Path output = config.getChangelogOutput();
    if( output != null ) {
      Files.createDirectories( output.getParent() );
      return Files.newBufferedWriter( output );
    }

    Log log = getLog();
    return new Writer() {

      private Pattern NL = Pattern.compile( "\\R" );

      private String remaining = "";

      @Override
      public void write( char[] cbuf, int off, int len ) {

        String w = remaining + new String( cbuf, off, len );

        int s = 0;
        Matcher matcher = NL.matcher( w );
        while( matcher.find() ) {
          log.info( w.substring( s, matcher.start() ) );
          s = matcher.end();
        }

        remaining = w.substring( s );

      }

      @Override
      public void flush() {
        log.info( remaining );
      }

      @Override
      public void close() {
        flush();
      }

    };

  }

}
