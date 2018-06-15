package fabuco.examples.camel;

import fabuco.process.ParameterKey;
import fabuco.process.ProcessParameter;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Persons implements ProcessParameter<String> {

  private int namesListUniqueId;
  private List<String> names;

  @Override
  public ParameterKey getKey() {
    return new ParameterKey("NAMES LIST UNIQUE ID", String.valueOf(namesListUniqueId));
  }
}
