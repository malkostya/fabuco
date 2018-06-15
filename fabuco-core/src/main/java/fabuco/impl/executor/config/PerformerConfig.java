package fabuco.impl.executor.config;

import com.typesafe.config.ConfigException;
import fabuco.impl.config.FabucoConfig;
import lombok.Getter;

@Getter
public class PerformerConfig extends FabucoConfig {

  private String code;
  private String dispatcher;
  private int activeCallsMaxNumber;

  @Override
  public void initConfigParameters() throws ConfigException {
    code = getString("code");
    dispatcher = getString("dispatcher");
    activeCallsMaxNumber = getInt("active-calls-max-number");
  }
}
