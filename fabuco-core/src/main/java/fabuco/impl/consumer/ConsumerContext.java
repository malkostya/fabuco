package fabuco.impl.consumer;

import akka.actor.ActorRef;
import fabuco.impl.config.FabucoConfigFactory;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.storage.FabucoStorage;
import fabuco.process.ProcessParameter;
import java.util.Map;

public interface ConsumerContext {

  Map<Class<ProcessParameter>, ActorRef> getCoordinators();

  FabucoConfigFactory getConfigFactory();

  FabucoStorage getStorage();

  Map<String, ProcessAttributes> getAttributesByCode();

}
