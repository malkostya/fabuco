package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg4;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg4Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg4Wrapper INSTANCE = new StepArg4Wrapper<>();

  private StepArg4<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg4Wrapper() {
  }

  private StepArg4Wrapper(StepArg4<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg4Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0], args[1], args[2], args[3]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg4Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg4.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 4;
  }
}
