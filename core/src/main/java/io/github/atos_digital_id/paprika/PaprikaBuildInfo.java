package io.github.atos_digital_id.paprika;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.inject.Named;
import javax.inject.Singleton;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Paprika plugin build info.
 **/
@Data
@Named
@Singleton
public class PaprikaBuildInfo {

  /**
   * Group id.
   *
   * @return the group id.
   **/
  private final String groupId;

  /**
   * Artifact id.
   *
   * @return the artifact id.
   **/
  private final String artifactId;

  /**
   * Version.
   *
   * @return the version.
   **/
  private final String version;

  @Getter( lazy = true )
  @EqualsAndHashCode.Exclude
  private final String string = this.groupId + ":" + this.artifactId + ":" + this.version;

  @Override
  public String toString() {
    return this.getString();
  }

  public PaprikaBuildInfo() {

    try(
        InputStream inputStream =
            getClass().getResourceAsStream( "/META-INF/build-info.properties" ) ) {

      Properties properties = new Properties();
      properties.load( inputStream );

      this.groupId = properties.getProperty( "project.groupId" );
      this.artifactId = properties.getProperty( "project.artifactId" );
      this.version = properties.getProperty( "project.version" );

    } catch( IOException e ) {
      throw new RuntimeException( "Can not load build info: " + e.getMessage(), e );
    }

  }

}
