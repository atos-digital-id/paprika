package io.github.atos_digital_id.paprika;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Paprika update pom Mojo. Exists only for technical reasons and should not be
 * called or used directly.
 **/
@Mojo( name = PaprikaUpdatePomMojo.GOAL, aggregator = true )
public class PaprikaUpdatePomMojo extends AbstractMojo {

  public static final String GOAL = "update-pom";

  @Parameter( defaultValue = "${project}", readonly = true, required = true )
  private MavenProject project;

  @Override
  public synchronized void execute() throws MojoExecutionException {

    try {

      File newPom = File.createTempFile( "pom.", ".paprika-maven-plugin.xml" );
      newPom.deleteOnExit();

      try( FileWriter writer = new FileWriter( newPom ) ) {
        new MavenXpp3Writer().write( writer, project.getModel() );
      }

      project.setPomFile( newPom );

    } catch( IOException ex ) {
      throw new MojoExecutionException( "Unable to modify pom: " + ex.getMessage(), ex );
    }

  }

}
