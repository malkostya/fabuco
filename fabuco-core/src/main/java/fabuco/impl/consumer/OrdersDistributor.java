package fabuco.impl.consumer;

import akka.actor.ActorRef;
import akka.actor.Props;
import fabuco.impl.consumer.config.SortedOrdersConfig;
import fabuco.impl.consumer.data.EmptyWorkerNotification;
import fabuco.impl.consumer.data.Order;
import fabuco.impl.consumer.data.OrdersContainer;
import fabuco.impl.core.AbstractBaseActor;
import fabuco.impl.core.FabucoConstants;
import fabuco.impl.util.counter.LongCounterAnalyzer;
import fabuco.process.ParameterKey;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class OrdersDistributor extends AbstractBaseActor {

  private final ConsumerContext context;
  private final String sortedGroup;
  private final Duration keepOrderBeforeSend;
  private final Map<ParameterKey, WorkerInfo> workers = new HashMap<>();
  private final Deque<WorkerInfo> freeWorkers = new ArrayDeque<>();
  private final LongCounterAnalyzer freeWorkersCounter = new LongCounterAnalyzer((short) 16, 20);
  private boolean freeWorkersBelowThreshold = true;

  public OrdersDistributor(ConsumerContext context, String sortedGroup) {
    this.context = context;
    this.sortedGroup = sortedGroup;
    SortedOrdersConfig config = context.getConfigFactory().getSortedOrdersConfig(sortedGroup);
    this.keepOrderBeforeSend = config.getKeepOrderBeforeSend();
    sendToItself(FabucoConstants.ONE_SECOND, FabucoConstants.ONE_SECOND, Tick.INSTANCE);
  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof EmptyWorkerNotification) {
      processEmptyWorker((EmptyWorkerNotification) message);
    } else if (message instanceof OrdersContainer) {
      processBunch((OrdersContainer) message);
    } else if (message instanceof Tick) {
      tickFreeWorkersCounter();
    }
  }

  private void tickFreeWorkersCounter() {
    if (!freeWorkersCounter.tickAndCheckForExceedingThreshold() && !freeWorkersBelowThreshold) {
      freeWorkersBelowThreshold = true;
    }
  }

  private void processEmptyWorker(EmptyWorkerNotification notification) {
    final Order order = notification.getOrder();
    final WorkerInfo workerInfo = workers.remove(order.getKey());

    if (workerInfo.getLastOrderId().equals(order.getOrderId())) {
      if (freeWorkersBelowThreshold) {
        freeWorkers.add(workerInfo);
        freeWorkersCounter.inc();
        if (freeWorkersCounter.checkForExceedingThreshold()) {
          freeWorkersBelowThreshold = false;
        }
      }
    } else {
      workers.put(order.getKey(), workerInfo);
    }
  }

  private void processBunch(OrdersContainer container) {
    container.getOrders().stream().forEach(order -> {
      WorkerInfo workerInfo = workers.get(order.getKey());

      if (workerInfo == null) {
        workerInfo = tryUsingFreeWorker(order.getKey());

        if (workerInfo == null) {
          workerInfo = createWorker(order.getKey());
        }
      }

      workerInfo.getWorkerRef().tell(order, getSelf());
      workerInfo.setLastOrderId(order.getOrderId());
    });
  }

  private WorkerInfo tryUsingFreeWorker(ParameterKey key) {
    final WorkerInfo workerInfo = freeWorkers.poll();

    if (workerInfo != null) {
      workers.put(key, workerInfo);
      freeWorkersCounter.dec();
    }

    return workerInfo;
  }

  private WorkerInfo createWorker(ParameterKey key) {
    final ActorRef workerRef = getContext().actorOf(
        Props.create(SortedOrdersWorker.class, context, key, sortedGroup, getSelf(),
            keepOrderBeforeSend));
    final WorkerInfo workerInfo = new WorkerInfo(workerRef);
    workers.put(key, workerInfo);
    return workerInfo;
  }

  static class WorkerInfo {

    final ActorRef workerRef;
    String lastOrderId;

    WorkerInfo(ActorRef workerRef) {
      this.workerRef = workerRef;
    }

    ActorRef getWorkerRef() {
      return workerRef;
    }

    void setLastOrderId(String lastOrderId) {
      this.lastOrderId = lastOrderId;
    }

    String getLastOrderId() {
      return lastOrderId;
    }
  }

  static class Tick implements Serializable {

    static final Tick INSTANCE = new Tick();
  }
}
