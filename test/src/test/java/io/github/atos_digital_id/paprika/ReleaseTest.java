package io.github.atos_digital_id.paprika;

import static java.util.Arrays.asList;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.github.atos_digital_id.paprika.GitProjectBuilder.Dep;
import io.github.atos_digital_id.paprika.GitProjectBuilder.ReleaseResult;

public class ReleaseTest {

  private GitProjectBuilder git;

  @BeforeEach
  public void setUp() throws IOException, GitAPIException {
    git = new GitProjectBuilder();
  }

  @AfterEach
  public void tearDown() throws IOException {
    if( git != null )
      git.close();
  }

  @Test
  public void testNotVersionned( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );

    git.testReleaseFail( info );

  }

  @Test
  public void testPristine( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );

    git.testRelease( info );

  }

  @Test
  public void testSnapshot( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );
    String last = git.commit( "New alpha" );

    git.testRelease(
        info,
        ReleaseResult.builder().artifactId( "alpha" ).version( "1.1.0" ).signed( false )
            .annotated( true ).message( "Release alpha 1.1.0" ).longCommit( last ).build() );

  }

  @Test
  public void testDirty( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );

    git.testReleaseFail( info );

  }

  @Test
  public void testDependantModules( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( new Dep( "alpha" ) ), asList() );
    git.java( "beta", 0, "beta" );
    String init = git.commit( "Init commit" );
    git.java( "beta", 1, "beta" );
    String commit = git.commit( "New beta" );

    git.testRelease(
        info,
        ReleaseResult.builder().artifactId( "parent" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release parent 0.1.0" ).longCommit( init ).build(),
        ReleaseResult.builder().artifactId( "alpha" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release alpha 0.1.0" ).longCommit( init ).build(),
        ReleaseResult.builder().artifactId( "beta" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release beta 0.1.0" ).longCommit( commit ).build() );

  }

  @Test
  public void testDependantModulesConfigLastModificationProperties( TestInfo info )
      throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( new Dep( "alpha" ) ), asList() );
    git.java( "beta", 0, "beta" );
    git.config( "beta", "release.lastModification", "false" );
    String init = git.commit( "Init commit" );
    git.java( "beta", 1, "beta" );
    git.commit( "New beta" );

    git.testRelease(
        info,
        ReleaseResult.builder().artifactId( "parent" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release parent 0.1.0" ).longCommit( init ).build(),
        ReleaseResult.builder().artifactId( "alpha" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release alpha 0.1.0" ).longCommit( init ).build(),
        ReleaseResult.builder().artifactId( "beta" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release beta 0.1.0" ).build() );

  }

  @Test
  public void testDependantModulesConfigLastModificationEnvironment( TestInfo info )
      throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( new Dep( "alpha" ) ), asList() );
    git.java( "beta", 0, "beta" );
    git.config( "beta", "release.lastModification", "false" );
    git.commit( "Init commit" );
    git.java( "beta", 1, "beta" );
    git.commit( "New beta" );

    git.setEnvVar( "PAPRIKA_RELEASE_LAST_MODIFICATION", "false" );

    git.testRelease(
        info,
        ReleaseResult.builder().artifactId( "parent" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release parent 0.1.0" ).build(),
        ReleaseResult.builder().artifactId( "alpha" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release alpha 0.1.0" ).build(),
        ReleaseResult.builder().artifactId( "beta" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release beta 0.1.0" ).build() );

  }

  @Test
  public void testSkipTagged( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( new Dep( "alpha" ) ), asList() );
    git.java( "beta", 0, "beta" );
    String init = git.commit( "Init commit" );
    git.java( "alpha", 1, "alpha" );
    git.java( "beta", 1, "beta" );
    String commit = git.commit( "New alpha & beta" );
    git.tag( "alpha/2.0.0" );

    git.testRelease(
        info,
        ReleaseResult.builder().artifactId( "parent" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release parent 0.1.0" ).longCommit( init ).build(),
        ReleaseResult.builder().artifactId( "beta" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release beta 0.1.0" ).longCommit( commit ).build() );

  }

  @Test
  public void testDontSkipTagged( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( new Dep( "alpha" ) ), asList() );
    git.java( "beta", 0, "beta" );
    String init = git.commit( "Init commit" );
    git.java( "alpha", 1, "alpha" );
    git.java( "beta", 1, "beta" );
    String commit = git.commit( "New alpha & beta" );
    git.tag( "alpha/2.0.0" );

    git.setEnvVar( "PAPRIKA_RELEASE_SKIP_TAGGED", "false" );

    git.testRelease(
        info,
        ReleaseResult.builder().artifactId( "parent" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release parent 0.1.0" ).longCommit( init ).build(),
        ReleaseResult.builder().artifactId( "alpha" ).version( "2.1.0" ).signed( false )
            .annotated( true ).message( "Release alpha 2.1.0" ).longCommit( commit ).build(),
        ReleaseResult.builder().artifactId( "beta" ).version( "0.1.0" ).signed( false )
            .annotated( true ).message( "Release beta 0.1.0" ).longCommit( commit ).build() );

  }

}
