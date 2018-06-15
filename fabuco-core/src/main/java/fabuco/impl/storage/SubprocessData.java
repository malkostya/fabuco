package fabuco.impl.storage;

import fabuco.impl.executor.FabucoProcessError;
import lombok.Getter;

@Getter
public class SubprocessData {

  private final String orderId;
  private final String resultAsString;
  private final FabucoProcessError errorInfo;

  public SubprocessData(String orderId) {
    this.orderId = orderId;
    this.resultAsString = null;
    this.errorInfo = null;
  }

  public SubprocessData(String orderId, String resultAsString, FabucoProcessError errorInfo) {
    this.orderId = orderId;
    this.resultAsString = resultAsString;
    this.errorInfo = errorInfo;
  }

  public boolean hasResult() {
    return resultAsString != null || errorInfo != null;
  }
}
