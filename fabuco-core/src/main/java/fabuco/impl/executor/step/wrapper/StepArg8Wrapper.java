package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg8;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg8Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg8Wrapper INSTANCE = new StepArg8Wrapper<>();

  private StepArg8<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg8Wrapper() {
  }

  private StepArg8Wrapper(StepArg8<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg8Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0], args[1], args[2], args[3], args[4], args[5],
        args[6], args[7]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg8Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg8.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 8;
  }
}
