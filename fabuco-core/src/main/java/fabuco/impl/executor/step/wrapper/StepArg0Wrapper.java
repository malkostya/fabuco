package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg0;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg0Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg0Wrapper INSTANCE = new StepArg0Wrapper<>();

  private StepArg0<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg0Wrapper() {
  }

  private StepArg0Wrapper(StepArg0<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg0Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg0Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg0.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args == null;
  }
}
