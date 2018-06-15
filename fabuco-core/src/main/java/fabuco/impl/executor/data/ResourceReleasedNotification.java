package fabuco.impl.executor.data;

import java.io.Serializable;

public class ResourceReleasedNotification implements Serializable {

  private static final long serialVersionUID = 4723678099604191511L;
  public static final ResourceReleasedNotification INSTANCE = new ResourceReleasedNotification();

  private ResourceReleasedNotification() {
  }
}
