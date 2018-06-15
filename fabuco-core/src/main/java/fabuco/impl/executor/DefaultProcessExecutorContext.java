package fabuco.impl.executor;

import static java.util.stream.Collectors.toMap;

import fabuco.process.FabucoProcess;
import fabuco.impl.executor.data.PerformerAttributes;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.step.StepUtils;
import fabuco.impl.executor.step.wrapper.StepWrapper;
import fabuco.impl.storage.FabucoStorage;
import fabuco.performer.PerformerParameter;
import fabuco.process.ProcessParameter;
import java.util.List;
import java.util.Map;

public class DefaultProcessExecutorContext implements ProcessExecutorContext {

  private final FabucoStorage storage;
  private final Map<Class<? extends ProcessParameter>, ProcessAttributes> processAttributesMap;
  private final Map<Class<? extends PerformerParameter>, PerformerAttributes> performerAttributesMap;
  private final Map<Class, Map<String, List<StepWrapper>>> stepsInfoByProcessMap;

  public DefaultProcessExecutorContext(FabucoStorage storage,
      Map<Class<? extends ProcessParameter>, ProcessAttributes> processAttributesMap,
      Map<Class<? extends PerformerParameter>, PerformerAttributes> performerAttributesMap) {
    this.storage = storage;
    this.processAttributesMap = processAttributesMap;
    this.performerAttributesMap = performerAttributesMap;
    this.stepsInfoByProcessMap = processAttributesMap.values().stream().collect(toMap(
        ProcessAttributes::getProcessType,
        info -> StepUtils.getProcessStepsInfo(info.getProcessType())
    ));
  }

  @Override
  public <P extends ProcessParameter<R>, R> ProcessAttributes<P, R> getProcessAttributes(
      Class<P> parameterType) {
    return processAttributesMap.get(parameterType);
  }

  @Override
  public <P extends PerformerParameter<R>, R> PerformerAttributes<P, R> getPerformerAttributes(
      Class<P> performerType) {
    return performerAttributesMap.get(performerType);
  }

  @Override
  public Map<String, List<StepWrapper>> getStepsInfoByProcess(
      Class<? extends FabucoProcess> processType) {
    return stepsInfoByProcessMap.get(processType);
  }

  @Override
  public FabucoStorage getStorage() {
    return storage;
  }
}
