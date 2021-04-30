package io.github.atos_digital_id.paprika;

import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Repository.shortenRefName;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.apache.maven.AbstractMavenLifecycleParticipant;
import org.apache.maven.MavenExecutionException;
import org.apache.maven.execution.MavenSession;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryBuilder;

import io.github.atos_digital_id.paprika.utils.Pretty;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import lombok.NonNull;

/**
 * Access point for paths of the project and Git tools.
 **/
@Named
@Singleton
public class GitHandler extends AbstractMavenLifecycleParticipant {

  @Inject
  private PaprikaLogger logger;

  private Repository repository;

  private ZonedDateTime startTime;

  private Path mavenRoot;

  private Path gitRoot;

  private Git git;

  private List<String> branches;

  private ObjectId head;

  @Override
  public void afterSessionStart( MavenSession session ) throws MavenExecutionException {

    startTime =
        ZonedDateTime.ofInstant( session.getStartTime().toInstant(), ZoneId.systemDefault() );

    mavenRoot =
        session.getRequest().getMultiModuleProjectDirectory().toPath().toAbsolutePath().normalize();
    logger.log( "Maven root: {}", mavenRoot );

    try {
      repository = new RepositoryBuilder().findGitDir( mavenRoot.toFile() ).build();
    } catch( IOException ex ) {
      throw new MavenExecutionException( "Can not load git environment: " + ex.getMessage(), ex );
    }

    gitRoot = repository.getWorkTree().toPath().toAbsolutePath().normalize();
    logger.log( "Git directory: {}", gitRoot );

    // Git
    git = new Git( repository );

    // Head
    try {
      head = repository.resolve( HEAD );
    } catch( IOException ex ) {
      throw new MavenExecutionException( "Can't find HEAD commit: " + ex.getMessage(), ex );
    }
    logger.log( "Head commit: {}", Pretty.id( head ) );

    // Current branch
    if( head == null ) {

      branches = Collections.emptyList();

    } else {
      try {

        Ref headRef = repository.getRefDatabase().exactRef( HEAD );
        if( headRef.isSymbolic() ) {
          // HEAD target a branch

          branches = Collections.singletonList( shortenRefName( headRef.getTarget().getName() ) );

        } else {
          // detached HEAD. Guess if a branch is targeted

          List<String> foundBranches = new ArrayList<>();

          for( Ref ref : repository.getRefDatabase().getRefsByPrefix( R_HEADS ) ) {
            ObjectId target = ref.getLeaf().getObjectId();
            if( head.equals( target ) )
              foundBranches.add( shortenRefName( ref.getName() ) );
          }

          // No targeted branch
          if( foundBranches.isEmpty() )
            foundBranches.add( shortenRefName( head.getName() ) );

          Collections.sort( foundBranches );

          branches = Collections.unmodifiableList( foundBranches );

        }

      } catch( IOException ex ) {
        throw new MavenExecutionException( "Can't find current branch: " + ex.getMessage(), ex );
      }
    }
    logger.log( "Current branches: {}", branches );

  }

  @Override
  public void afterSessionEnd( MavenSession session ) throws MavenExecutionException {

    if( repository != null )
      repository.close();

    repository = null;
    git = null;

  }

  private <T> T checkinit( T obj ) {
    if( repository == null )
      throw new IllegalStateException( "Git repository not loaded yet." );
    return obj;
  }

  /**
   * Returns the current repository.
   *
   * @return the current repository.
   **/
  public Repository repository() {
    return checkinit( this.repository );
  }

  /**
   * Returns the Maven session starting date.
   *
   * @return the Maven session starting date.
   **/
  public ZonedDateTime startTime() {
    return checkinit( this.startTime );
  }

  /**
   * Returns the Maven multi-module project root path.
   *
   * @return the Maven multi-module project root path.
   **/
  public Path mavenRoot() {
    return checkinit( this.mavenRoot );
  }

  /**
   * Returns the Git repository root path.
   *
   * @return the Git repository root path.
   **/
  public Path gitRoot() {
    return checkinit( this.gitRoot );
  }

  /**
   * Construct a path between the Git repository root path and the given path.
   * Directory separators are {@code /}.
   *
   * @param other the path to relativize.
   * @return the relativized path.
   * @see GitHandler#resolve
   **/
  public String relativize( @NonNull Path other ) {
    return Repository
        .stripWorkDir( repository().getWorkTree(), other.toAbsolutePath().normalize().toFile() );
  }

  /**
   * Resolve a path from the Git repository root path.
   *
   * @param path the path to resolve.
   * @return the absolute and normalized resolved path.
   * @see GitHandler#relativize
   **/
  public Path resolve( @NonNull String path ) {
    return repository.getWorkTree().toPath().resolve( path ).toAbsolutePath().normalize();
  }

  /**
   * Returns the access point to Git API.
   *
   * @return the access point to Git API.
   **/
  public Git git() {
    return checkinit( this.git );
  }

  /**
   * Returns the current Git branch names. Potentially several branch names can
   * be found.
   *
   * @return the current Git branch names.
   **/
  public List<String> branches() {
    return checkinit( this.branches );
  }

  /**
   * Returns the commit id of {@code HEAD}.
   *
   * @return the commit id of {@code HEAD}.
   **/
  public ObjectId head() {
    return checkinit( this.head );
  }

}
