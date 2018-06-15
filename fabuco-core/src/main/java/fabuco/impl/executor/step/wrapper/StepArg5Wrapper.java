package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg5;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg5Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg5Wrapper INSTANCE = new StepArg5Wrapper<>();

  private StepArg5<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg5Wrapper() {
  }

  private StepArg5Wrapper(StepArg5<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg5Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0], args[1], args[2], args[3], args[4]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg5Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg5.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 5;
  }
}
