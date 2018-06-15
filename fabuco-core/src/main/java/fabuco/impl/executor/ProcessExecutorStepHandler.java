package fabuco.impl.executor;

import akka.actor.ActorRef;
import fabuco.impl.exception.InvalidProcessException;
import fabuco.impl.executor.data.FabucoStepResult;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.step.wrapper.StepWrapper;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public abstract class ProcessExecutorStepHandler<P extends ProcessParameter<R>, R>
    extends ProcessExecutorInvoker<P, R> {

  private Map<String, List<StepWrapper>> stepsInfo;
  private FabucoStepResult currentStepResult;

  public ProcessExecutorStepHandler(ProcessExecutorContext context,
      ProcessAttributes<P, R> processAttributes, ActorRef invoker) {
    super(context, processAttributes, invoker);
    stepsInfo = context.getStepsInfoByProcess(this.attributes.getProcessType());
  }

  @Override
  protected void onReceiveMessage(Object message) throws Exception {
    if (message instanceof StepResult) {
      currentStepResult = (FabucoStepResult) message;
      if (isStepComplete()) {
        nextStep();
      }
    } else {
      super.onReceiveMessage(message);
    }

  }

  @Override
  protected void handleStepResult(StepResult stepResult) {
    if (stepResult != null) {
      currentStepResult = null;
      if (stepResult != FINAL_STEP) {
        if (stepResult.getDelay() == null) {
          sendToItself(stepResult);
        } else {
          sendToItself(stepResult.getDelay(), stepResult);
        }
      }
    } else {
      throw new InvalidProcessException("Empty step result");
    }
  }

  @Override
  protected void nextStep() {
    if (currentStepResult != null) {
      List<StepWrapper> stepsWrappers = stepsInfo.get(currentStepResult.getMethodName());
      if (stepsWrappers != null) {
        Optional<StepWrapper> stepWrapper = stepsWrappers.stream().filter(sa -> sa.
            validate(currentStepResult.getArgs())).findFirst();
        if (stepWrapper.isPresent()) {
          final StepResult stepResult = stepWrapper.get()
              .apply(process, this, currentStepResult.getArgs());
          handleStepResult(stepResult);
        } else {
          throw new InvalidProcessException(
              "Parameters of the method " + currentStepResult.getMethodName() + " don't match.");
        }
      } else {
        throw new InvalidProcessException(
            "Wrong name of the method " + currentStepResult.getMethodName());
      }
    }
  }

  @Override
  public StepResult stepTo(String methodName, Object... methodArgs) {
    return new FabucoStepResult(methodName, methodArgs);
  }

  @Override
  public StepResult stepTo(String methodName) {
    return new FabucoStepResult(methodName);
  }

}
