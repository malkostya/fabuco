package fabuco.examples.camel;

import fabuco.process.ParameterKey;
import fabuco.process.ProcessParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GreetPerson implements ProcessParameter<String> {

  private String personName;

  @Override
  public ParameterKey getKey() {
    return new ParameterKey("PERSON NAME", personName);
  }

}
