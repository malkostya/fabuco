package fabuco.impl.consumer.data;

import java.io.Serializable;
import lombok.Value;

@Value
public class WorkerOrder implements Serializable {

  private static final long serialVersionUID = 8154535849420539333L;
  private final Order order;
  private long startTick;

  public long getSortedDate() {
    return order.getSortedDate();
  }
}
