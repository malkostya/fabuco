package fabuco.impl.consumer.data;

import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.process.ParameterKey;
import fabuco.process.ProcessParameter;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode(of = {"orderId"})
public class Order<P extends ProcessParameter<R>, R> implements Serializable {

  private static final long serialVersionUID = 2272181101760876763L;
  private final String orderId;
  private final int priority;
  private final long expired;
  private final long sortedDate; // equals 0 for unsorted orders
  private final ParameterKey key;
  private final String processAttrsCode;
  private transient ProcessAttributes<P, R> attributes;
  private transient long consumed; // date when an order is consumed by worker

  public Order(String orderId, int priority, long expired, long sortedDate, ParameterKey key,
      String processAttrsCode) {
    this.orderId = orderId;
    this.priority = priority;
    this.expired = expired;
    this.sortedDate = sortedDate;
    this.key = key;
    this.processAttrsCode = processAttrsCode;
  }

  public void setAttributes(ProcessAttributes<P, R> attributes) {
    this.attributes = attributes;
  }
}
