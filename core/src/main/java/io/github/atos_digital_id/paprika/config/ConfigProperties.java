package io.github.atos_digital_id.paprika.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import lombok.NonNull;

/**
 * Properties file manager.
 **/
@Named
@Singleton
public class ConfigProperties {

  @Inject
  private PaprikaLogger logger;

  @Inject
  private GitHandler gitHandler;

  private final Map<Path, Map<String, String>> propsMap = new HashMap<>();

  /**
   * Gets the properties file content of a directory.
   *
   * @param path path of the directory.
   * @return the configuration of the directory.
   **/
  public Map<String, String> get( @NonNull Path path ) {

    logger.log( "Load {}", path );

    Map<String, String> properties = propsMap.get( path );
    if( properties != null ) {
      logger.log( "Cached: {}", properties );
      return properties;
    }

    properties = new HashMap<>();

    Path parent = path.getParent();
    if( parent.startsWith( gitHandler.mavenRoot() ) ) {
      logger.log( "Look at parent {}", parent );
      properties.putAll( get( parent ) );
      logger.log( "Initialized {}", properties );
    }

    Path configFile = path.resolve( ".mvn" ).resolve( "paprika.properties" );
    logger.log( "Look at file {}", configFile );
    if( Files.exists( configFile ) ) {

      try( InputStream in = Files.newInputStream( configFile ) ) {
        Properties props = new Properties();
        props.load( in );
        for( String name : props.stringPropertyNames() )
          properties.put( name, props.getProperty( name ) );
      } catch( IOException ex ) {
        throw new IllegalArgumentException(
            "Can not load configuration file at " + configFile + ": " + ex.getMessage(),
            ex );
      }

    }

    logger.log( "Loaded properties: {}", properties );

    propsMap.put( path, properties );
    return properties;

  }

}
