package fabuco.impl.consumer.data;

import java.io.Serializable;


public class BelowThresholdNotification implements Serializable {

  private static final long serialVersionUID = 6278992027686676292L;
  public static final BelowThresholdNotification INSTANCE = new BelowThresholdNotification();

  private BelowThresholdNotification() {
  }
}
