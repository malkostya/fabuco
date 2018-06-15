package fabuco.process;

import java.io.Serializable;
import lombok.Value;

@Value
public class ParameterKey implements Serializable {

  private final String keyType;
  private final String keyId;
}
