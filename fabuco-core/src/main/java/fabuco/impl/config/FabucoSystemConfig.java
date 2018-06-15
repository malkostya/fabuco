package fabuco.impl.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import java.io.Serializable;
import lombok.Getter;

@Getter
public class FabucoSystemConfig {

  private final String nodeId;
  private final String scanPathPrefix;

  public FabucoSystemConfig(Config config) throws ConfigException {
    nodeId = config.getString("node-id");
    scanPathPrefix = config.getString("scan-path-prefix");
  }
}
