package fabuco.impl.executor;

import fabuco.process.FabucoProcess;
import fabuco.impl.executor.data.PerformerAttributes;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.step.wrapper.StepWrapper;
import fabuco.impl.storage.FabucoStorage;
import fabuco.performer.PerformerParameter;
import fabuco.process.ProcessParameter;
import java.util.List;
import java.util.Map;

public interface ProcessExecutorContext {

  <P extends ProcessParameter<R>, R> ProcessAttributes<P, R> getProcessAttributes(
      Class<P> parameterType);

  <P extends PerformerParameter<R>, R> PerformerAttributes<P, R> getPerformerAttributes(
      Class<P> performerType);

  Map<String, List<StepWrapper>> getStepsInfoByProcess(Class<? extends FabucoProcess> processType);

  FabucoStorage getStorage();
}
