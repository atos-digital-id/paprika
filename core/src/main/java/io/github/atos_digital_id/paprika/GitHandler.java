package io.github.atos_digital_id.paprika;

import static org.eclipse.jgit.lib.Constants.HEAD;
import static org.eclipse.jgit.lib.Constants.R_HEADS;
import static org.eclipse.jgit.lib.Constants.R_REMOTES;
import static org.eclipse.jgit.lib.Repository.shortenRefName;

import java.io.IOException;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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

import io.github.atos_digital_id.paprika.config.Config;
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

  private String branch;

  private ObjectId head;

  @Override
  public void afterSessionStart( MavenSession session ) throws MavenExecutionException {

    if( Config.isSkipped() )
      return;

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
    branch = currentBranch();
    logger.log( "Current branches: {}", branch );

  }

  private String currentBranch() throws MavenExecutionException {

    if( head == null )
      return "";

    try {

      Ref headRef = repository.getRefDatabase().exactRef( HEAD );

      // HEAD target a branch
      if( headRef.isSymbolic() )
        return shortenRefName( headRef.getTarget().getName() );

      // detached HEAD. Guess if a branch is targeted

      for( Ref ref : repository.getRefDatabase().getRefsByPrefix( R_HEADS ) ) {
        ObjectId target = ref.getLeaf().getObjectId();
        if( head.equals( target ) ) {
          String shortened = shortenRefName( ref.getName() );
          if( !HEAD.equals( shortened ) )
            return shortened;
        }
      }

      for( Ref ref : repository.getRefDatabase().getRefsByPrefix( R_REMOTES ) ) {
        ObjectId target = ref.getLeaf().getObjectId();
        if( head.equals( target ) ) {
          String shortened = repository.shortenRemoteBranchName( ref.getName() );
          if( !HEAD.equals( shortened ) )
            return shortened;
        }
      }

      // No targeted branch
      return shortenRefName( head.getName() );

    } catch( IOException ex ) {
      throw new MavenExecutionException( "Can't find current branch: " + ex.getMessage(), ex );
    }

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
  public String branch() {
    return checkinit( this.branch );
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
