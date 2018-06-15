package fabuco.impl.consumer;

import static java.util.stream.Collectors.toMap;

import akka.actor.ActorRef;
import akka.actor.Props;
import fabuco.impl.core.AbstractBaseActor;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ConsumerManager extends AbstractBaseActor {

  private final Map<String, ActorRef> consumers = new HashMap<>();

  public ConsumerManager(final ConsumerContext context, List<String> keySetIds) {
    Map<String, ActorRef> consumersOfGroup = keySetIds.stream().collect(toMap(
        Function.identity(),
        keySetId -> createConsumer(context, keySetId)
    ));

    consumers.putAll(consumersOfGroup);
  }

  @Override
  public void onReceive(Object message) {
    // TODO: implement Add/Remove KeySet commands for add/remove nodes

  }

  private ActorRef createConsumer(ConsumerContext context, String keySetId) {
    return getContext()
        .actorOf(Props.create(OrdersConsumer.class, context, keySetId));
  }
}
