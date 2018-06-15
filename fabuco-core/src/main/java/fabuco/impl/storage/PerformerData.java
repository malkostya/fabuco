package fabuco.impl.storage;

import lombok.Getter;

@Getter
public class PerformerData {

  private final String orderId;
  private final String resultAsString;
  private final String faultCode;
  private final Throwable recoverableError;

  public PerformerData(String orderId) {
    this.orderId = orderId;
    this.resultAsString = null;
    this.faultCode = null;
    this.recoverableError = null;
  }

  public PerformerData(String orderId, String resultAsString, String faultCode,
      Throwable recoverableError) {
    this.orderId = orderId;
    this.resultAsString = resultAsString;
    this.faultCode = faultCode;
    this.recoverableError = recoverableError;
  }

  public boolean hasResult() {
    return resultAsString != null || faultCode != null || recoverableError != null;
  }
}
