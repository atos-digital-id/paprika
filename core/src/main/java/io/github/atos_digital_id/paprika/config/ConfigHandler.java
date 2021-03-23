package io.github.atos_digital_id.paprika.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import io.github.atos_digital_id.paprika.utils.cache.ArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.cache.HashMapArtifactIdCache;
import io.github.atos_digital_id.paprika.utils.log.PaprikaLogger;
import lombok.NonNull;

/**
 * Configuration manager.
 **/
@Named
@Singleton
public class ConfigHandler {

  private static final String KEY_INIT_VERSION = "initVersion";
  private static final String KEY_OBSERVED_PATH = "observedPath";
  private static final String KEY_NON_QUALIFIER_BRANCHE_NAMES = "nonQualifierBranches";
  private static final String KEY_REPRODUCIBLE = "reproducible";

  private static final String KEY_ANNOTATED = "release.annotated";
  private static final String KEY_LAST_MODIF = "release.lastModification";
  private static final String KEY_MESSAGE = "release.message";
  private static final String KEY_SIGNED = "release.signed";
  private static final String KEY_RELEASE_IGNORE = "release.ignore";

  @Inject
  private PaprikaLogger logger;

  @Inject
  private GitHandler gitHandler;

  private final ArtifactIdCache<Config> configs = new HashMapArtifactIdCache<>();

  private final Map<Path, Map<String, String>> propsMap = new HashMap<>();

  /**
   * Gets the configuration of a module.
   *
   * @param def the configured module.
   * @return the configuration of the module.
   **/
  public Config get( @NonNull ArtifactDef def ) {
    return configs.get( def, () -> this.internalGet( def ) );
  }

  private Map<String, String> load( Path workingDir ) {

    logger.log( "Load {}", workingDir );

    Map<String, String> properties = propsMap.get( workingDir );
    if( properties != null ) {
      logger.log( "Cached: {}", properties );
      return properties;
    }

    properties = new HashMap<>();

    Path parent = workingDir.getParent();
    if( parent.startsWith( gitHandler.mavenRoot() ) ) {
      logger.log( "Look at parent {}", parent );
      properties.putAll( load( parent ) );
      logger.log( "Initialized {}", properties );
    }

    Path configFile = workingDir.resolve( ".mvn" ).resolve( "paprika.properties" );
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

    logger.log( "Loaded: {}", properties );

    propsMap.put( workingDir, properties );
    return properties;

  }

  private Config internalGet( ArtifactDef def ) {

    logger.reset( "Loading configuration of {}: ", def );

    try {

      Map<String, String> properties = load( def.getWorkingDir() );

      Config.ConfigBuilder builder = Config.builder();

      readConf( properties, KEY_INIT_VERSION, builder::initVersionValue );
      readConf( properties, KEY_OBSERVED_PATH, builder::observedPathValue );
      readConf( properties, KEY_NON_QUALIFIER_BRANCHE_NAMES, builder::nonQualifierBrancheNames );
      readConf( properties, KEY_REPRODUCIBLE, builder::reproducibleValue );

      readConf( properties, KEY_LAST_MODIF, builder::lastModificationValue );
      readConf( properties, KEY_ANNOTATED, builder::annotatedValue );
      readConf( properties, KEY_MESSAGE, builder::releaseMessage );
      readConf( properties, KEY_RELEASE_IGNORE, builder::releaseIgnoredValue );
      readConf( properties, KEY_SIGNED, builder::signedValue );

      return builder.build();

    } finally {
      logger.restore();
    }

  }

  private void readConf( Map<String, String> properties, String key, Consumer<String> setter ) {
    if( properties.containsKey( key ) ) {
      String value = properties.get( key ).toString();
      logger.log( "Set {} = {}", key, value );
      setter.accept( value );
    }
  }

}
