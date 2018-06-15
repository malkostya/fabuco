package fabuco.impl.executor;

import fabuco.process.ProcessError;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class FabucoProcessError implements ProcessError {

  String errorMessage;

  @Override
  public String getMessage() {
    return errorMessage;
  }
}
