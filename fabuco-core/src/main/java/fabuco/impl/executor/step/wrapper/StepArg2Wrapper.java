package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.function.StepArg2;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public class StepArg2Wrapper<P extends ProcessParameter<R>, R> implements StepWrapper<P, R> {

  private static final StepArg2Wrapper INSTANCE = new StepArg2Wrapper<>();

  private StepArg2<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function;

  private StepArg2Wrapper() {
  }

  private StepArg2Wrapper(StepArg2<StepResult, FabucoProcess<P, R>, ProcessExecutor<R>> function) {
    this.function = function;
  }

  public static StepArg2Wrapper getInstance() {
    return INSTANCE;
  }

  @Override
  public StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args) {
    return function.apply(process, executor, args[0], args[1]);
  }

  @Override
  public StepWrapper<P, R> getWrapperForMethod(Method method) {
    return new StepArg2Wrapper<>(InvokeMethodUtils.createLambda(method, StepArg2.class, "apply"));
  }

  @Override
  public boolean validate(Object[] args) {
    return args.length == 2;
  }
}
