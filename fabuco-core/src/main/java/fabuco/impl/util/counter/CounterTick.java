package fabuco.impl.util.counter;

import java.io.Serializable;

public class CounterTick implements Serializable {

  private static final long serialVersionUID = 425151297653309331L;
  public static final CounterTick INSTANCE = new CounterTick();
}
