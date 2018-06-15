package fabuco.impl.executor;

import akka.actor.ActorRef;
import fabuco.impl.exception.InvalidProcessException;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.data.SubProcessCompleteResponse;
import fabuco.impl.executor.data.SubProcessFailResponse;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FabucoProcessExecutor<P extends ProcessParameter<R>, R>
    extends ProcessExecutorStepHandler<P, R> {

  public FabucoProcessExecutor(ProcessExecutorContext context,
      ProcessAttributes<P, R> processAttributes, ActorRef invoker) {
    super(context, processAttributes, invoker);
  }

  @Override
  public void onReceive(Object message) throws Exception {
    try {
      super.onReceiveMessage(message);
    } catch (Exception e) {
      log.error("orderId: " + orderId + " details: " + e.getMessage(), e);
      throw e;
    }
  }

  @Override
  public StepResult complete(R result) {
    checkFinishingEligibility();
    if (!isRoot) {
      saveSubProcessCompleted(result);
      notifyInvokerAboutCompleted(result);
    } else {
      saveProcessCompleted(result);
    }
    stopActor();
    return FINAL_STEP;
  }

  @Override
  public StepResult fail(String errorMessage) {
    checkFinishingEligibility();
    FabucoProcessError errorInfo = new FabucoProcessError(errorMessage);
    if (!isRoot) {
      saveSubProcessFailed(errorInfo);
      notifyInvokerAboutFailed(errorInfo);
    } else {
      saveProcessFailed(errorInfo);
    }
    stopActor();
    return FINAL_STEP;
  }

  private void checkFinishingEligibility() {
    if (!isStepComplete()) {
      throw new InvalidProcessException("You should wait for the results of subprocesses before "
          + "complete your order.");
    }
  }

  private void notifyInvokerAboutCompleted(R result) {
    sendToInvoker(new SubProcessCompleteResponse<>(childAddress, result));
  }

  private void notifyInvokerAboutFailed(FabucoProcessError errorInfo) {
    sendToInvoker(new SubProcessFailResponse<>(childAddress, errorInfo));
  }

  private void saveProcessCompleted(R result) {
    storage.saveProcessCompleted(valueToString(result), orderId);
  }

  private void saveProcessFailed(FabucoProcessError errorInfo) {
    storage.saveProcessFailed(errorInfo, orderId);
  }
  /**
   * The method saves the complete state of subprocess in remote storage
   */
  private void saveSubProcessCompleted(R result) {
    storage.saveSubProcessCompleted(valueToString(result), root, parentId, attributes.getCode(),
        childAddress.getIndex());
  }

  /**
   * The method saves the failed state of subprocess in remote storage
   */
  private void saveSubProcessFailed(FabucoProcessError errorInfo) {
    storage.saveSubProcessFailed(errorInfo, root, parentId, attributes.getCode(),
        childAddress.getIndex());
  }
}
