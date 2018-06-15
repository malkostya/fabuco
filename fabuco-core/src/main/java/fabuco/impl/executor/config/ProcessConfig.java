package fabuco.impl.executor.config;

import com.typesafe.config.ConfigException;
import fabuco.impl.config.FabucoConfig;
import lombok.Getter;

@Getter
public class ProcessConfig extends FabucoConfig {

  private String code;
  private String sortedGroup;
  private String dispatcher;
  private int activeOrdersMaxNumber;

  @Override
  public void initConfigParameters() throws ConfigException {
    code = getString("code");
    sortedGroup = getString("sorted-group");
    dispatcher = getString("dispatcher");
    activeOrdersMaxNumber = getInt("active-orders-max-number");
  }
}
