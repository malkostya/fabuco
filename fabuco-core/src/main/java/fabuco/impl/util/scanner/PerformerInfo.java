package fabuco.impl.util.scanner;

import java.lang.reflect.Method;
import lombok.Value;

@Value
public class PerformerInfo {

  private final Object performer;
  private final Method initMethod;
  private final Method closeMethod;
}
