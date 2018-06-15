package fabuco.impl.executor;

import fabuco.performer.PerformerCall;
import fabuco.performer.PerformerCallContext;
import fabuco.performer.PerformerCallState;
import fabuco.performer.PerformerParameter;

public class FabucoPerformerCall<P extends PerformerParameter<R>, R> implements
    PerformerCall<P, R>, PerformerCallContext<P, R> {

  private PerformerCallState state;
  private P parameter;
  private R result;
  private String faultCode;
  private Throwable recoverableError;

  public FabucoPerformerCall(P parameter) {
    this.state = PerformerCallState.IN_PROGRESS;
    this.parameter = parameter;
  }

  public void clear() {
    this.state = PerformerCallState.IN_PROGRESS;
    result = null;
    faultCode = null;
    recoverableError = null;
  }

  @Override
  public void setCompleted(R result) {
    state = PerformerCallState.SUCCEED;
    this.result = result;
  }

  @Override
  public void setExpired() {
    state = PerformerCallState.EXPIRED;
  }

  @Override
  public void setExpiredWithoutTry() {
    state = PerformerCallState.EXPIRED_WITHOUT_TRY;
  }

  @Override
  public void setFailed(String faultCode) {
    state = PerformerCallState.IRRECOVERABLE_ERROR;
    this.faultCode = faultCode;
  }

  @Override
  public void setRecoverableError(Throwable error) {
    state = PerformerCallState.RECOVERABLE_ERROR;
    this.recoverableError = error;
  }

  @Override
  public PerformerCallState getState() {
    return state;
  }

  @Override
  public boolean isSucceeded() {
    return state == PerformerCallState.SUCCEED;
  }

  @Override
  public boolean isRecoverableError() {
    return state == PerformerCallState.RECOVERABLE_ERROR;
  }

  @Override
  public boolean isExpired() {
    return state == PerformerCallState.EXPIRED;
  }

  @Override
  public P getParameter() {
    return parameter;
  }

  @Override
  public R getResult() {
    return result;
  }

  @Override
  public String getFaultCode() {
    return faultCode;
  }

  @Override
  public Throwable getRecoverableError() {
    return recoverableError;
  }
}
