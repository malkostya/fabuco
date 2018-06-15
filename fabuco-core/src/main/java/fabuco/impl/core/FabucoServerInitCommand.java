package fabuco.impl.core;

import java.io.Serializable;

public class FabucoServerInitCommand implements Serializable {

  private static final long serialVersionUID = -7338929739222806265L;
  public static final FabucoServerInitCommand INSTANCE = new FabucoServerInitCommand();

  private FabucoServerInitCommand() {
  }
}
