package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg9;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg9Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg9Wrapper INSTANCE = new StepArg9Wrapper<>();

  private StepArg9<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg9Wrapper() {
  }

  private StepArg9Wrapper(StepArg9<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg9Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0], args[1], args[2], args[3], args[4], args[5],
        args[6], args[7], args[8]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg9Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg9.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 9;
  }
}
