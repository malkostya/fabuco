package fabuco.impl.core;

import static java.util.stream.Collectors.toMap;

import akka.actor.ActorRef;
import akka.actor.Props;
import fabuco.impl.config.FabucoConfigFactory;
import fabuco.impl.config.FabucoSystemConfig;
import fabuco.impl.consumer.ConsumerManager;
import fabuco.impl.consumer.DefaultConsumerContext;
import fabuco.impl.executor.DefaultProcessExecutorContext;
import fabuco.impl.executor.PerformerCoordinator;
import fabuco.impl.executor.ProcessCoordinator;
import fabuco.impl.executor.config.PerformerConfig;
import fabuco.impl.executor.config.ProcessConfig;
import fabuco.impl.executor.data.PerformerAttributes;
import fabuco.impl.executor.data.PerformerCoordinatorInitCommand;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.data.ProcessCoordinatorInitCommand;
import fabuco.impl.storage.FabucoStorage;
import fabuco.impl.storage.FabucoStorageFactory;
import fabuco.impl.util.scanner.FabucoScanner;
import fabuco.impl.util.scanner.PerformerInfo;
import fabuco.impl.util.scanner.PerformerInfos;
import fabuco.impl.util.scanner.PerformerMethodInfo;
import fabuco.impl.util.scanner.ProcessInfo;
import fabuco.performer.PerformerParameter;
import fabuco.process.ProcessParameter;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

public class FabucoServer extends AbstractBaseActor {

  private static final String defaultDispatcher = "akka.actor.default-dispatcher";
  private static final FabucoConfigFactory configFactory = new FabucoConfigFactory();
  private FabucoStorage storage;
  private FabucoSystemConfig systemConfig;
  private DefaultProcessExecutorContext executorContext;
  private DefaultConsumerContext consumerContext;
  private ActorRef consumerManager;
  private List<PerformerInfo> performerInfoList;
  private FabucoScanner scanner;

  @Override
  public void onReceive(Object message) {
    if (message instanceof FabucoServerInitCommand) {
      storage = new FabucoStorageFactory().getStorage();
      systemConfig = configFactory.getSystemConfig();
      scanner = new FabucoScanner(systemConfig.getScanPathPrefix());

      final Map<Class<? extends PerformerParameter>, PerformerAttributes> performerAttributesMap =
          getPerformerAttributesMap();
      final Map<Class<? extends ProcessParameter>, ProcessAttributes> processAttributesMap =
          getProcessAttributesMap();

      initPerformers();
      initCoordinators(processAttributesMap, performerAttributesMap);
      startConsumers(processAttributesMap);
    } else if (message instanceof FabucoServerCloseCommand) {
      closePerformers();
      getContext().getSystem().terminate();
    }
  }

  private void initPerformers() {
    performerInfoList.stream().forEach(info -> {
      try {
        info.getInitMethod().invoke(info.getPerformer());
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void closePerformers() {
    performerInfoList.stream().forEach(info -> {
      try {
        info.getCloseMethod().invoke(info.getPerformer());
      } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
        throw new RuntimeException(e);
      }
    });
  }

  private void initCoordinators(
      Map<Class<? extends ProcessParameter>, ProcessAttributes> processAttributesMap,
      Map<Class<? extends PerformerParameter>, PerformerAttributes> performerAttributesMap) {
    executorContext = new DefaultProcessExecutorContext(storage, processAttributesMap,
        performerAttributesMap);
    performerAttributesMap.values().stream().forEach(attrs ->
        attrs.getCoordinatorRef()
            .tell(new PerformerCoordinatorInitCommand(attrs, storage), getSelf())
    );
    processAttributesMap.values().stream().forEach(attrs ->
        attrs.getCoordinatorRef()
            .tell(new ProcessCoordinatorInitCommand(attrs, executorContext), getSelf())
    );
  }

  private void startConsumers(
      Map<Class<? extends ProcessParameter>, ProcessAttributes> processAttributesMap) {
    final List<String> keySetIds = storage.getKeySetIdsByNode(systemConfig.getNodeId());
    consumerContext = new DefaultConsumerContext(configFactory, storage, processAttributesMap);
    consumerManager = getContext().actorOf(
        Props.create(ConsumerManager.class, consumerContext, keySetIds));
  }

  private Map<Class<? extends ProcessParameter>, ProcessAttributes> getProcessAttributesMap() {
    return scanner.getProcessInfos().stream().collect(toMap(
        ProcessInfo::getParameterType,
        info -> {
          final ProcessConfig config = configFactory
              .getProcessParametersConfig(info.getParameterType());
          final String dispatcher = getProcessParameterDispatcher(config);
          final ActorRef coordinator = createProcessCoordinator(config.getActiveOrdersMaxNumber(),
              dispatcher);
          return new ProcessAttributes(getProcessParameterCode(info.getParameterType(), config),
              info.getParameterType(), info.getResultType(), info.getProcessType(), coordinator,
              config.getSortedGroup(), dispatcher);
        }
    ));
  }

  private Map<Class<? extends PerformerParameter>, PerformerAttributes> getPerformerAttributesMap() {
    final PerformerInfos infos = scanner.getPerformerInfos();
    performerInfoList = infos.getPerformerInfoList();
    return infos.getPerformerMethodInfos().stream().collect(toMap(
        PerformerMethodInfo::getParameterType,
        info -> {
          final PerformerConfig config = configFactory
              .getPerformerParametersConfig(info.getParameterType());
          final String dispatcher = getPerformerParameterDispatcher(config);
          ActorRef coordinator = createPerformerCoordinator(config.getActiveCallsMaxNumber(),
              dispatcher);
          return new PerformerAttributes(
              getPerformerParameterCode(info.getParameterType(), config),
              info.getParameterType(), info.getResultType(), info.getPerformer(),
              info.getFunction(), coordinator, dispatcher);
        }
    ));
  }

  private String getProcessParameterCode(Class parameterType, ProcessConfig config) {
    final String code = config.getCode();
    return code != null ? code : parameterType.getName();
  }

  private String getProcessParameterDispatcher(ProcessConfig config) {
    final String dispatcher = config.getDispatcher();
    return dispatcher != null ? dispatcher : defaultDispatcher;
  }

  private String getPerformerParameterCode(Class parameterType, PerformerConfig config) {
    final String code = config.getCode();
    return code != null ? code : parameterType.getName();
  }

  private String getPerformerParameterDispatcher(PerformerConfig config) {
    final String dispatcher = config.getDispatcher();
    return dispatcher != null ? dispatcher : defaultDispatcher;
  }

  private ActorRef createProcessCoordinator(int activeOrdersMaxNumber, String dispatcher) {
    return getContext().actorOf(Props.create(ProcessCoordinator.class, activeOrdersMaxNumber)
        .withDispatcher(dispatcher));
  }

  private ActorRef createPerformerCoordinator(int activeCallsMaxNumber, String dispatcher) {
    return getContext().actorOf(Props.create(PerformerCoordinator.class, activeCallsMaxNumber)
        .withDispatcher(dispatcher));
  }
}
