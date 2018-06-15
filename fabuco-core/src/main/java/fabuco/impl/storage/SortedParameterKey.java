package fabuco.impl.storage;

import fabuco.process.ParameterKey;
import lombok.Value;

@Value
public class SortedParameterKey {

  private final ParameterKey parameterKey;
  private final String sortedGroup;
}
