package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg3;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg3Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg3Wrapper INSTANCE = new StepArg3Wrapper<>();

  private StepArg3<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg3Wrapper() {
  }

  private StepArg3Wrapper(StepArg3<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg3Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0], args[1], args[2]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg3Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg3.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 3;
  }
}
