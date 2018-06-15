package fabuco.impl.config;

import java.util.Map;
import lombok.Value;

@Value
public class FabucoConfigInfo<T extends FabucoConfig> {

  private final T defaultConfig;
  private final Map<String, T> configs;

}
