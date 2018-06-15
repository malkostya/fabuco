package fabuco.impl.executor;

import fabuco.impl.executor.data.ProcessState;
import fabuco.process.ProcessParameter;
import fabuco.process.ProcessError;
import fabuco.process.ProcessCall;

public class FabucoProcessCall<P extends ProcessParameter<R>, R> implements
    ProcessCall<P, R> {

  private ProcessState state = ProcessState.IN_PROGRESS;
  private P parameter;
  private R result;
  private FabucoProcessError error;

  public FabucoProcessCall(P parameter) {
    this.parameter = parameter;
  }

  public void setCompleted(R result) {
    state = ProcessState.COMPLETE;
    this.result = result;
  }

  public void setFailed(FabucoProcessError error) {
    state = ProcessState.FAIL;
    this.error = error;
  }

  @Override
  public boolean succeeded() {
    return state == ProcessState.COMPLETE;
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
  public ProcessError getError() {
    return error;
  }
}
