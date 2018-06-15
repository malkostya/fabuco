package fabuco.impl.executor.step.wrapper;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.lang.reflect.Method;

public interface StepWrapper<P extends ProcessParameter<R>, R> {

  StepResult apply(FabucoProcess<P, R> process, ProcessExecutor<R> executor, Object[] args);

  StepWrapper<P, R> getWrapperForMethod(Method method);

  boolean validate(Object[] args);
}
