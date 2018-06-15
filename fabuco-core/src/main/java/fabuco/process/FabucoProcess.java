package fabuco.process;

import fabuco.step.StepResult;

public interface FabucoProcess<P extends ProcessParameter<R>, R> {

  StepResult onStart(ProcessExecutor<R> pe, P parameter);
}
