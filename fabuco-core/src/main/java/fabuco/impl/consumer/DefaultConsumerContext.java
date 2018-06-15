package fabuco.impl.consumer;

import static java.util.stream.Collectors.toMap;

import akka.actor.ActorRef;
import fabuco.impl.config.FabucoConfigFactory;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.storage.FabucoStorage;
import fabuco.process.ProcessParameter;
import java.util.Map;
import java.util.function.Function;
import lombok.Getter;

@Getter
public class DefaultConsumerContext implements ConsumerContext {

  private final FabucoConfigFactory configFactory;
  private final FabucoStorage storage;
  private final Map<Class<ProcessParameter>, ActorRef> coordinators;
  private final Map<String, ProcessAttributes> attributesByCode;

  public DefaultConsumerContext(FabucoConfigFactory configFactory, FabucoStorage storage,
      Map<Class<? extends ProcessParameter>, ProcessAttributes> processAttributesMap) {
    this.configFactory = configFactory;
    this.storage = storage;
    this.coordinators = processAttributesMap.values().stream().collect(toMap(
        ProcessAttributes::getParameterType,
        ProcessAttributes::getCoordinatorRef
    ));
    this.attributesByCode = processAttributesMap.values().stream().collect(toMap(
        ProcessAttributes::getCode,
        Function.identity()
    ));
  }
}
