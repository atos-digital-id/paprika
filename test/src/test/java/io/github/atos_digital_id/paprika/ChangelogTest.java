package io.github.atos_digital_id.paprika;

import static java.util.Arrays.asList;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import io.github.atos_digital_id.paprika.GitProjectBuilder.ChangelogResult;
import io.github.atos_digital_id.paprika.GitProjectBuilder.Dep;

public class ChangelogTest {

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
  public void simpleTest( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "init-commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );
    git.commit( "new-alpha" );

    git.testChangelog(
        info,
        null,
        ChangelogResult.builder().message( "new-alpha" ).build(),
        ChangelogResult.builder().version( "1.0.0" ).message( "init-commit" ).build() );

  }

  @Test
  public void extractExactReleasesTest( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "alpha", 0, null, "jar", asList(), asList(), asList() );
    git.java( ".", 0, "alpha" );
    git.commit( "init-commit" );
    git.tag( "alpha/1.0.0" );
    git.java( ".", 1, "alpha" );
    git.commit( "alpha-1" );
    git.java( ".", 2, "alpha" );
    git.commit( "alpha-2" );
    git.java( ".", 3, "alpha" );
    git.commit( "alpha-3" );
    git.tag( "alpha/1.1.0" );
    git.java( ".", 4, "alpha" );
    git.commit( "alpha-4" );
    git.java( ".", 5, "alpha" );
    git.commit( "alpha-5" );
    git.java( ".", 6, "alpha" );
    git.commit( "alpha-6" );
    git.tag( "alpha/1.2.0" );
    git.java( ".", 7, "alpha" );
    git.commit( "alpha-7" );

    git.testChangelog( info, verifier -> {
      verifier.addCliArgument( "-Dfrom=1.0.0" );
      verifier.addCliArgument( "-Dto=1.2.0" );
    },
        ChangelogResult.builder().version( "1.2.0" ).message( "alpha-4" ).message( "alpha-5" )
            .message( "alpha-6" ).build(),
        ChangelogResult.builder().version( "1.1.0" ).message( "alpha-1" ).message( "alpha-2" )
            .message( "alpha-3" ).build() );

  }

  @Test
  public void dependenciesTest( TestInfo info ) throws Exception {

    git.readme( ".", 0 );
    git.pom( ".", "parent", 0, null, "pom", asList( "alpha", "beta" ), asList(), asList() );
    git.pom( "alpha", "alpha", 0, "parent", "jar", asList(), asList(), asList() );
    git.java( "alpha", 0, "alpha" );
    git.pom( "beta", "beta", 0, "parent", "jar", asList(), asList( new Dep( "alpha" ) ), asList() );
    git.java( "beta", 0, "beta" );
    git.commit( "Init commit" );
    git.tag( "parent/1.0.0" );
    git.tag( "alpha/1.0.0" );
    git.tag( "beta/1.0.0" );
    git.java( "beta", 1, "beta" );
    git.commit( "beta-1" );
    git.java( "alpha", 1, "alpha" );
    git.commit( "alpha-1" );
    git.java( "alpha", 2, "alpha" );
    git.commit( "alpha-2" );
    git.java( "beta", 2, "beta" );
    git.commit( "beta-2" );
    git.tag( "alpha/1.1.0" );
    git.tag( "beta/2.0.0" );

    git.testChangelog( info, verifier -> {
      verifier.addCliArgument( "--projects" );
      verifier.addCliArgument( "alpha" );
    },
        ChangelogResult.builder().version( "1.1.0" ).message( "alpha-1" ).message( "alpha-2" )
            .build() );

    git.testChangelog( info, verifier -> {
      verifier.addCliArgument( "--projects" );
      verifier.addCliArgument( "beta" );
    },
        ChangelogResult.builder().version( "2.0.0" ).message( "beta-1" ).message( "alpha-1" )
            .message( "alpha-2" ).message( "beta-2" ).build() );

  }

}
