package fabuco.process;

import fabuco.impl.exception.InvalidProcessException;
import fabuco.impl.executor.step.StepUtils;
import fabuco.impl.executor.step.wrapper.StepWrapper;
import fabuco.performer.PerformerCall;
import fabuco.performer.PerformerInterceptor;
import fabuco.performer.PerformerParameter;
import fabuco.performer.RetryAlgorithm;
import fabuco.step.StepResult;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

/**
 * The class serves to help testing fabuco processes. It's still under development.
 */
@Slf4j
public class FakeProcessExecutor implements ProcessExecutor {
  private final FabucoProcess process;
  private final Map<String, List<StepWrapper>> stepsInfo;

  public FakeProcessExecutor(FabucoProcess process) {
    this.process = process;
    this.stepsInfo = StepUtils.getProcessStepsInfo(process.getClass());
  }

  @Override
  public StepResult stepTo(String methodName) {
    return stepTo(methodName, null);
  }

  @Override
  public StepResult stepTo(String methodName, Object... methodArgs) {
    List<StepWrapper> stepsWrappers = stepsInfo.get(methodName);
    if (stepsWrappers != null) {
      Optional<StepWrapper> stepWrapper = stepsWrappers.stream().filter(sa -> sa.
          validate(methodArgs)).findFirst();
      if (stepWrapper.isPresent()) {
        stepWrapper.get().apply(process, this, methodArgs);
      } else {
        throw new InvalidProcessException(
            "Parameters of the method " + methodName + " don't match.");
      }
    } else {
      throw new InvalidProcessException(
          "Wrong name of the method " + methodName);
    }

    return null;
  }

  @Override
  public void setDefaultPerformerCallInterceptor(
      PerformerInterceptor defaultPerformerCallInterceptor) {

  }

  @Override
  public void setDefaultRetryAlgorithm(RetryAlgorithm defaultRetryAlgorithm) {

  }

  @Override
  public StepResult complete(Object result) {
    return null;
  }

  @Override
  public StepResult fail(String errorMessage) {
    return null;
  }

  @Override
  public PerformerCall getPerformerCall(Class parameterType, String index) {
    return null;
  }

  @Override
  public PerformerCall getPerformerCall(Class parameterType) {
    return null;
  }

  @Override
  public String perform(PerformerParameter parameter, String index, Duration lifetime,
      PerformerInterceptor validator) {
    return null;
  }

  @Override
  public String perform(PerformerParameter parameter, String index,
      PerformerInterceptor validator) {
    return null;
  }

  @Override
  public String perform(PerformerParameter parameter, PerformerInterceptor validator) {
    return null;
  }

  @Override
  public String perform(PerformerParameter parameter, String index, Duration lifetime) {
    return null;
  }

  @Override
  public String perform(PerformerParameter parameter, String index) {
    return null;
  }

  @Override
  public String perform(PerformerParameter parameter) {
    return null;
  }

  @Override
  public ProcessCall getProcessCall(Class parameterType, String index) {
    return null;
  }

  @Override
  public ProcessCall getProcessCall(Class parameterType) {
    return null;
  }

  @Override
  public String process(ProcessParameter parameter, String index, LocalDateTime expired) {
    return null;
  }

  @Override
  public String process(ProcessParameter parameter, String index) {
    return null;
  }

  @Override
  public String process(ProcessParameter parameter) {
    return null;
  }
}
