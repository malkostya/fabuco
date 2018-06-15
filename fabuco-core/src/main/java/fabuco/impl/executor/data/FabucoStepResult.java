package fabuco.impl.executor.data;

import fabuco.step.StepResult;
import java.time.Duration;
import lombok.Getter;

@Getter
public class FabucoStepResult implements StepResult {

  public static final StepResult INSTANCE = new FabucoStepResult();
  private String methodName;
  private Object[] args;
  private Duration delay;

  private FabucoStepResult() {
  }

  public FabucoStepResult(String methodName) {
    this.methodName = methodName;
  }

  public FabucoStepResult(String methodName, Object[] args) {
    this.methodName = methodName;
    this.args = args;
  }

  @Override
  public StepResult withDelay(Duration delay) {
    this.delay = delay;
    return this;
  }

  @Override
  public Duration getDelay() {
    return delay;
  }
}
