package io.github.atos_digital_id.paprika;

import static io.github.atos_digital_id.paprika.GitProjectBuilder.GROUP_ID;
import static java.util.Arrays.asList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

import org.apache.maven.it.Verifier;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import io.github.atos_digital_id.paprika.GitProjectBuilder.Dep;
import io.github.atos_digital_id.paprika.GitProjectBuilder.Plugin;
import lombok.Data;

public class InstallTest {

  public static final Dep ALPHA = new Dep( "alpha" );

  @Data
  public static class ArtifactResult {

    private final String artifactId;

    private final String version;

    private final String packaging;

  }

  @Rule
  public TestName name = new TestName();

  private void install( ArtifactResult ... results ) throws Exception {

    Path workingDir = git.getWorkingDir();
    Path logPath = workingDir.resolve( ".log" );

    Verifier verifier = new Verifier( workingDir.toString(), true );
    verifier.setEnvironmentVariable( "PAPRIKA_LOGS", "TRACE" );

    for( ArtifactResult res : results )
      verifier.deleteArtifacts( GROUP_ID, res.getArtifactId(), res.getVersion() );
    verifier.setLogFileName( workingDir.relativize( logPath ).toString() );

    try {

      verifier.executeGoal( "install" );

      for( ArtifactResult res : results )
        verifier.assertArtifactPresent(
            GROUP_ID,
            res.getArtifactId(),
            res.getVersion(),
            res.getPackaging() );

      verifier.verifyErrorFreeLog();
      verifier.resetStreams();

    } finally {

      if( Files.exists( logPath ) ) {
        Files.lines( logPath ).forEach( l -> {
          System.out.println( "[ " + name.getMethodName() + " ] " + l );
        } );
      } else {
        System.out
            .println( "[ " + name.getMethodName() + " ] --- Log file " + logPath + " not found." );
      }

    }

  }

  private GitProjectBuilder git;

  @Before
  public void setUp() throws IOException, GitAPIException {
    git = new GitProjectBuilder();
  }

  @After
  public void tearDown() throws IOException {
    if( git != null )
      git.close();
  }

  @Test
  public void testNotVersionned() throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );

    install( new ArtifactResult( "alpha", "0.1.0-SNAPSHOT", "pom" ) );

  }

  @Test
  public void testPristine() throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );

    install( new ArtifactResult( "alpha", "1.0.0", "jar" ) );

  }

  @Test
  public void testSnapshot() throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );
    git.commit( "New alpha" );

    install( new ArtifactResult( "alpha", "1.1.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testDirty() throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );

    install( new ArtifactResult( "alpha", "1.1.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testGitIgnored() throws Exception {

    git.readme( ".", 0 );
    git.write( ".gitignore", "ignored\n" );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.write( "src/main/java/ignored", "Ignore me.\n" );

    install( new ArtifactResult( "alpha", "1.0.0", "jar" ) );

  }

  @Test
  public void testPristineModules() throws Exception {

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

    install(
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.1.0", "jar" ) );

  }

  @Test
  public void testDependantModules() throws Exception {

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

    install(
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.2.0-SNAPSHOT", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testOutdatingModules() throws Exception {

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

    install(
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.2.0", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testWrongUsage() throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( ALPHA ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.1.0" );
    git.tag( "beta/1.1.0" );

    install(
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "0.1.0-SNAPSHOT", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testLightweightTag() throws Exception {

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

    install(
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.2.0-SNAPSHOT", "jar" ) );

  }

  @Test
  public void testBranch() throws Exception {

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

    install(
        new ArtifactResult( "parent", "1.0.0", "pom" ),
        new ArtifactResult( "alpha", "0.1.0-SNAPSHOT.feature-my-great-feature", "jar" ),
        new ArtifactResult( "beta", "0.1.0-SNAPSHOT.feature-my-great-feature", "jar" ) );

  }

  @Test
  public void testFlatten() throws Exception {

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

    install(
        new ArtifactResult( "parent", "1.1.0", "pom" ),
        new ArtifactResult( "alpha", "1.1.0", "jar" ),
        new ArtifactResult( "beta", "1.1.0", "jar" ) );

  }

  @Test
  public void testDetachedTargetedHead() throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "Init commit" );
    git.tag( "alpha/1.0.0" );
    git.branch( "feature/my-great-feature" );
    git.java( ".", 1, "alpha" );
    String commit = git.commit( "New alpha" );
    git.checkout( commit );

    install( new ArtifactResult( "alpha", "1.1.0-SNAPSHOT.feature-my-great-feature", "jar" ) );

  }

  @Test
  public void testDetachedHead() throws Exception {

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

    install( new ArtifactResult( "alpha", "1.1.0-SNAPSHOT." + commit, "jar" ) );

  }

}
