package fabuco.impl.consumer.data;

import java.io.Serializable;
import lombok.Value;

@Value
public class EmptyWorkerNotification implements Serializable {

  private static final long serialVersionUID = 3134575425022800081L;
  private final Order order;
}
