package fabuco.impl.consumer;

import static java.util.stream.Collectors.toMap;

import akka.actor.ActorRef;
import akka.actor.Props;
import fabuco.impl.consumer.data.BelowThresholdNotification;
import fabuco.impl.consumer.data.ConsumerBunch;
import fabuco.impl.consumer.data.ExceededThresholdNotification;
import fabuco.impl.consumer.data.Order;
import fabuco.impl.consumer.data.OrdersContainer;
import fabuco.impl.core.AbstractBaseActor;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.storage.FabucoStorage;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class OrdersConsumer extends AbstractBaseActor {

  private static final long COEF = 100000; //TODO: remove after testing
  private static final GetOrderBunchesTick GET_ORDER_BUNCHES_TICK = new GetOrderBunchesTick(
      Duration.ofMillis(100L * COEF));
  private static final GetOrderBunchesTick RETRY_GET_ORDER_BUNCHES_TICK = new GetOrderBunchesTick(
      Duration.ofMillis(1000L * COEF));
  private final String keySetId;
  private final FabucoStorage storage;
  private final Map<String, ActorRef> distributors;
  private final Map<String, ProcessAttributes> attributesByCode;
  private long lastTime;
  private boolean keepConsuming = true;

  public OrdersConsumer(ConsumerContext context, String keySetId) {
    this.storage = context.getStorage();
    this.keySetId = keySetId;

    attributesByCode = context.getAttributesByCode();
    distributors = context.getConfigFactory().getSortedGroups().stream().collect(toMap(
        Function.identity(),
        sortedGroup -> getContext()
            .actorOf(Props.create(OrdersDistributor.class, context, sortedGroup))
    ));

    lastTime = 0;
    // TODO: fix after testing
//    sendTick(GET_ORDER_BUNCHES_TICK);
    sendToItself(Duration.ofMillis(10), GET_ORDER_BUNCHES_TICK);

  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof GetOrderBunchesTick) {
      requestOrderBunches();
    } else if (message instanceof ConsumerBunch) {
      final ConsumerBunch bunch = (ConsumerBunch) message;
      processConsumerBunch(bunch);
      lastTime = bunch.getLastTime();
      sendTick(GET_ORDER_BUNCHES_TICK);
    } else if (message instanceof ExceededThresholdNotification) {
      keepConsuming = false;
    } else if (message instanceof BelowThresholdNotification) {
      keepConsuming = true;
    }
  }

  private void requestOrderBunches() {
    if (keepConsuming) {
      asyncCall(() -> {
        final ConsumerBunch bunch = storage.getOrderBunch(keySetId, lastTime);
        if (bunch != null) {
          sendToItself(bunch);
        }
        return null;
      });
    } else {
      sendTick(RETRY_GET_ORDER_BUNCHES_TICK);
    }
  }

  private void processConsumerBunch(ConsumerBunch bunch) {
    List<Order> orders = new ArrayList<>();
    String sortedGroupOfDistributor = "";
    for (Order order : bunch.getOrders()) {
      ProcessAttributes attributes = attributesByCode.get(order.getProcessAttrsCode());
      order.setAttributes(attributes);
      if (order.getAttributes().inSortedGroup()) {
        if (!sortedGroupOfDistributor.equals(order.getAttributes().getSortedGroup())) {
          if (!sortedGroupOfDistributor.isEmpty()) {
            sendToDistributor(sortedGroupOfDistributor, orders);
          }
          sortedGroupOfDistributor = order.getAttributes().getSortedGroup();
          orders = new ArrayList<>();
        }
        orders.add(order);
      } else {
        sendToCoordinator(order);
      }
    }
    if (!sortedGroupOfDistributor.isEmpty()) {
      sendToDistributor(sortedGroupOfDistributor, orders);
    }
  }

  private void sendToCoordinator(Order order) {
    final ActorRef coordinatorRef = order.getAttributes().getCoordinatorRef();
    coordinatorRef.tell(order, getSelf());
  }

  private void sendToDistributor(String sortedGroup, List<Order> orders) {
    distributors.get(sortedGroup).tell(new OrdersContainer(orders), getSelf());
  }

  private void sendTick(GetOrderBunchesTick tick) {
    sendToItself(tick.delay, tick);
  }

  static class GetOrderBunchesTick implements Serializable {

    final Duration delay;

    GetOrderBunchesTick(Duration delay) {
      this.delay = delay;
    }
  }
}
