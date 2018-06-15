package fabuco.impl.core;

import java.io.Serializable;

public class FabucoServerCloseCommand implements Serializable {

  private static final long serialVersionUID = 6211983037232132688L;
  public static final FabucoServerCloseCommand INSTANCE = new FabucoServerCloseCommand();

  private FabucoServerCloseCommand() {
  }
}
