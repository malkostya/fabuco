package fabuco.impl.consumer.data;

import java.io.Serializable;

public class ExceededThresholdNotification implements Serializable {

  private static final long serialVersionUID = -3604822878776435018L;
  public static final ExceededThresholdNotification INSTANCE = new ExceededThresholdNotification();

  private ExceededThresholdNotification() {
  }
}
