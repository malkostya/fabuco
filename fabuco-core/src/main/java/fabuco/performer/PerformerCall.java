package fabuco.performer;

public interface PerformerCall<P extends PerformerParameter<R>, R> {

  PerformerCallState getState();

  boolean isSucceeded();

  boolean isExpired();

  P getParameter();

  R getResult();

  boolean isRecoverableError();

  String getFaultCode();

  Throwable getRecoverableError();

}
