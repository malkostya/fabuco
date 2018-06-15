package fabuco.impl.consumer.data;

import java.io.Serializable;
import java.util.List;
import lombok.Value;

@Value
public class ConsumerBunch implements Serializable {

  private static final long serialVersionUID = 7343907199380932265L;
  private final long lastTime;
  private final List<Order> orders;
}
