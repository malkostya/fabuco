package fabuco.examples.camel;

import fabuco.performer.PerformerParameter;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetGreeting implements PerformerParameter<String> {

  private String name;
}
