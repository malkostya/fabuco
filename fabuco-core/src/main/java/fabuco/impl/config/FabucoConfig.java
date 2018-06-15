package fabuco.impl.config;

import com.typesafe.config.Config;
import java.io.Serializable;
import java.time.Duration;

public abstract class FabucoConfig {

  private Config config;
  private Config defaultConfig;

  public void init(Config config, Config defaultConfig) {
    this.config = config;
    this.defaultConfig = defaultConfig;
    initConfigParameters();
  }

  protected abstract void initConfigParameters();

  protected String getString(String path) {
    if (config != null && config.hasPath(path)) {
      return config.getString(path);
    }
    if (defaultConfig.hasPath(path)) {
      return defaultConfig.getString(path);
    }
    return null;
  }

  protected Integer getInt(String path) {
    if (config != null && config.hasPath(path)) {
      return config.getInt(path);
    }
    if (defaultConfig.hasPath(path)) {
      return defaultConfig.getInt(path);
    }
    return null;
  }

  protected Long getLong(String path) {
    if (config != null && config.hasPath(path)) {
      return config.getLong(path);
    }
    if (defaultConfig.hasPath(path)) {
      return defaultConfig.getLong(path);
    }
    return null;
  }

  protected Duration getDuration(String path) {
    if (config != null && config.hasPath(path)) {
      return config.getDuration(path);
    }
    if (defaultConfig.hasPath(path)) {
      return defaultConfig.getDuration(path);
    }
    return null;
  }
}
