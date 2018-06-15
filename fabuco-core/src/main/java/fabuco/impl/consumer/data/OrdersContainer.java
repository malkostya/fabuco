package fabuco.impl.consumer.data;

import java.io.Serializable;
import java.util.List;
import lombok.Value;

@Value
public class OrdersContainer implements Serializable {

  private static final long serialVersionUID = -3207718392370593133L;
  private final List<Order> orders;
}
