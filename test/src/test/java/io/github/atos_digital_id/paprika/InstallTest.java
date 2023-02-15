package io.github.atos_digital_id.paprika;

import static java.util.Arrays.asList;

import java.io.IOException;
import java.util.HashMap;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.github.atos_digital_id.paprika.GitProjectBuilder.ArtifactResult;
import io.github.atos_digital_id.paprika.GitProjectBuilder.Dep;
import io.github.atos_digital_id.paprika.GitProjectBuilder.Plugin;

public class InstallTest {

  public static final Dep ALPHA = new Dep( "alpha" );

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

    git.testInstall( info, new ArtifactResult( "alpha", "0.1.0-SNAPSHOT", "pom" ) );

  }

  @Test
  public void testPristine( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );

    git.testInstall( info, new ArtifactResult( "alpha", "1.0.0", "jar" ) );

  }

  @Test
  public void testSnapshot( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );
    git.commit( "New alpha" );

    git.testInstall( info, new ArtifactResult( "alpha", "1.1.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testDirty( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );

    git.testInstall( info, new ArtifactResult( "alpha", "1.1.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testGitIgnored( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.write( ".gitignore", "ignored\n" );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.write( "src/main/java/ignored", "Ignore me.\n" );

    git.testInstall( info, new ArtifactResult( "alpha", "1.0.0", "jar" ) );

  }

  @Test
  public void testAddModule( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Add beta" );
    git.tag( "beta/1.1.0" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.1.0", "jar" ) );

  }

  @Test
  public void testPristineModules( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.tag( "beta/1.1.0" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.1.0", "jar" ) );

  }

  @Test
  public void testDependantModules( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.tag( "beta/1.1.0" );
    git.java( "alpha", 1, "alpha" );
    git.commit( "New alpha" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.2.0-SNAPSHOT", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testOutdatingModules( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.tag( "beta/1.1.0" );
    git.java( "alpha", 1, "alpha" );
    git.commit( "New alpha" );
    git.tag( "alpha/1.2.0" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.2.0", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testWrongUsage( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "beta/1.1.0" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "0.1.0-SNAPSHOT", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testLightweightTag( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.lightweightTag( "parent/1.1.0" );
    git.lightweightTag( "alpha/1.1.0" );
    git.lightweightTag( "beta/1.1.0" );
    git.java( "beta", 1, "beta" );
    git.commit( "New beta" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testBranch( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.0.0" );
    git.branch( "feature/my-great-feature" );
    git.java( "beta", 1, "beta" );
    git.commit( "New beta" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.0.0", "pom" ),
        new ArtifactResult( "alpha", "0.1.0-SNAPSHOT.feature-my-great-feature", "jar" ),
        new ArtifactResult( "beta", "0.1.0-SNAPSHOT.feature-my-great-feature", "jar" ) );

  }

  @Test
  public void testFlatten( TestInfo info ) throws Exception {

    Plugin flatten = new Plugin(
        "org.codehaus.mojo",
        "flatten-maven-plugin",
        "1.2.7",
        new HashMap<>(),
        "process-resources",
        "flatten" );

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList( flatten ) );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.tag( "beta/1.1.0" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.1.0", "jar" ) );

  }

  @Test
  public void testDetachedTargetedHead( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.branch( "feature/my-great-feature" );
    git.java( ".", 1, "alpha" );
    String commit = git.commit( "New alpha" );
    git.checkout( commit );

    git.testInstall(
        info,
        new ArtifactResult( "alpha", "1.1.0-SNAPSHOT.feature-my-great-feature", "jar" ) );

  }

  @Test
  public void testDetachedHead( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.branch( "feature/my-great-feature" );
    git.java( ".", 1, "alpha" );
    String commit = git.commit( "New alpha" );
    git.java( ".", 2, "alpha" );
    git.commit( "Another new alpha" );
    git.checkout( commit );

    git.testInstall( info, new ArtifactResult( "alpha", "1.1.0-SNAPSHOT." + commit, "jar" ) );

  }

  @Test
  public void testDashedSuffix( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "alpha-beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "alpha-beta", "alpha-beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "alpha-beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.tag( "alpha-beta/1.1.0" );
    git.java( "alpha", 1, "alpha" );
    git.commit( "New alpha" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.2.0-SNAPSHOT", "jar" ),
        new ArtifactResult( "alpha-beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testRemoveModule( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "alpha/1.1.0" );
    git.tag( "beta/1.1.0" );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha" ), asList(), asList() );
    git.rm( "beta" );

    git.testInstall(
        info,
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ) );

  }

}
