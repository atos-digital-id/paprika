package io.github.atos_digital_id.paprika.config;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import io.github.atos_digital_id.paprika.GitHandler;
import io.github.atos_digital_id.paprika.project.ArtifactDef;
import lombok.NonNull;

/**
 * Configuration manager.
 **/
@Named
@Singleton
public class ConfigHandler {

  @Inject
  private GitHandler gitHandler;

  @Inject
  private ConfigProperties configProperties;

  private final Map<Path, Config> configs = new HashMap<>();

  /**
   * Gets the configuration of a directory.
   *
   * @param dir the directory.
   * @return the configuration of the directory.
   **/
  public Config get( @NonNull Path dir ) {
    return configs.computeIfAbsent( dir, path -> new Config( configProperties, dir ) );
  }

  /**
   * Gets the configuration of the root directory.
   *
   * @return the configuration of the module.
   **/
  public Config get() {
    return get( gitHandler.mavenRoot() );
  }

  /**
   * Gets the configuration of a module.
   *
   * @param def the configured module.
   * @return the configuration of the module.
   **/
  public Config get( @NonNull ArtifactDef def ) {
    return get( def.getWorkingDir() );
  }

}
