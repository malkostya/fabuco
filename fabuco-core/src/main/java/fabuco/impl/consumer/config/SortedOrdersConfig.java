package fabuco.impl.consumer.config;

import com.typesafe.config.ConfigException;
import fabuco.impl.config.FabucoConfig;
import java.time.Duration;
import lombok.Getter;

@Getter
public class SortedOrdersConfig extends FabucoConfig {

  private Duration keepOrderBeforeSend;

  @Override
  public void initConfigParameters() throws ConfigException {
    keepOrderBeforeSend = getDuration("keep_order_before_send");
  }
}
