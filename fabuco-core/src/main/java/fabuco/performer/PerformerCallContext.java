package fabuco.performer;

public interface PerformerCallContext<P extends PerformerParameter<R>, R> {

  P getParameter();

  void setCompleted(R result);

  void setExpired();

  void setExpiredWithoutTry();

  void setFailed(String faultCode);

  void setRecoverableError(Throwable error);

}
