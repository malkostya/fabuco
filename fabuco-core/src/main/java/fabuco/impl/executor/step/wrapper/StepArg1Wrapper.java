package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg1;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg1Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg1Wrapper INSTANCE = new StepArg1Wrapper<>();

  private StepArg1<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg1Wrapper() {
  }

  private StepArg1Wrapper(StepArg1<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg1Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg1Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg1.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 1;
  }
}
