package fabuco.impl.consumer;

import static java.util.Comparator.comparing;

import akka.actor.ActorRef;
import fabuco.impl.consumer.data.EmptyWorkerNotification;
import fabuco.impl.consumer.data.Order;
import fabuco.impl.consumer.data.WorkerOrder;
import fabuco.impl.core.AbstractBaseActor;
import fabuco.impl.executor.data.ResourceReleasedNotification;
import fabuco.impl.storage.FabucoStorage;
import fabuco.impl.storage.SortedParameterKey;
import fabuco.impl.core.FabucoConstants;
import fabuco.impl.util.counter.IntCounterAnalyzer;
import fabuco.process.ParameterKey;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class SortedOrdersWorker extends AbstractBaseActor {

  private static final int KEPT_ORDERS_MAX_SIZE = 32;
  private static final int SAVING_BUFFER_MAX_SIZE = KEPT_ORDERS_MAX_SIZE << 1;
  private final SortedParameterKey sortedParameterKey;
  private final ActorRef distributor;
  private final FabucoStorage storage;
  private final Duration keepOrderBeforeSend; // max delay of all order types of sorted group
  private ProcessingState processingState = ProcessingState.FREE;
  private Order processingOrder;
  private Order lastOrder;
  private WorkKeeper keeper;

  public SortedOrdersWorker(ConsumerContext context, ParameterKey parameterKey, String sortedGroup,
      ActorRef distributor, Duration keepOrderBeforeSend) {
    this.sortedParameterKey = new SortedParameterKey(parameterKey, sortedGroup);
    this.distributor = distributor;
    this.storage = context.getStorage();
    this.keepOrderBeforeSend = keepOrderBeforeSend;
  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof Order) {
      processOrder((Order) message);
    } else if (message instanceof ResourceReleasedNotification) {
      resourceReleased();
    } else if (message instanceof KeepOrderTick) {
      if (keeper == null) {
        sendToCoordinator();
      }
    } else if (message instanceof CounterTick) {
      keeper.tick();
    } else if (message instanceof SavedOrdersNotification) {
      keeper.handleSavedOrdersNotification((SavedOrdersNotification) message);
    } else if (message instanceof GetOrdersResponse) {
      keeper.handleGetOrdersResponse((GetOrdersResponse) message);
    }
  }

  private void processOrder(Order order) {
    lastOrder = order;
    if (processingState == ProcessingState.FREE) {
      processingOrder = order;
      if (keepOrderBeforeSend != null) {
        processingState = ProcessingState.WAIT;
        if (keeper == null) {
          sendToItself(keepOrderBeforeSend, KeepOrderTick.INSTANCE);
        } else {
          keeper.setProcessingOrderTick();
        }
      } else {
        sendToCoordinator();
      }
    } else {
      if (keeper == null) {
        long tickNumberBeforeSend = processingState == ProcessingState.WAIT
            ? keepOrderBeforeSend.getSeconds()
            - (System.currentTimeMillis() - processingOrder.getConsumed()) / 1000
            : 0;
        keeper = new WorkKeeper(tickNumberBeforeSend);
      }
      keeper.keepOrder(order);
    }
  }

  private void resourceReleased() {
    if (keeper == null) {
      finishProcessing();
    } else {
      keeper.resourceReleased();
    }
  }

  private void finishProcessing() {
    processingState = ProcessingState.FREE;
    processingOrder = null;
    notifyDistributorOfWorkerIsEmpty();
  }

  private void sendToCoordinator() {
    final ActorRef coordinatorRef = processingOrder.getAttributes().getCoordinatorRef();
    coordinatorRef.tell(processingOrder, getSelf());
    processingState = ProcessingState.SENT;
  }

  private void notifyDistributorOfWorkerIsEmpty() {
    distributor.tell(new EmptyWorkerNotification(lastOrder), getSelf());
  }

  class WorkKeeper {

    TreeSet<WorkerOrder> ordersQueue;
    List<WorkerOrder> savingBuffer;
    IntCounterAnalyzer counterAnalyzer;
    WorkerOrder lastInQueue;
    long tickCount = 0;
    long tickNumberBeforeSend;
    int saveInStorageCallsNumber = 0;
    long numberOfSavedInStorage = 0;
    boolean doKeepInQueue = true;
    boolean isBlockedByGet = false;
    boolean needToGet = false;

    WorkKeeper(long tickNumberBeforeSend) {
      this.tickNumberBeforeSend = tickNumberBeforeSend;
      ordersQueue = new TreeSet<>(comparing(WorkerOrder::getSortedDate));
      counterAnalyzer = new IntCounterAnalyzer((short) 10, KEPT_ORDERS_MAX_SIZE);
      sendToItself(FabucoConstants.ONE_SECOND, FabucoConstants.ONE_SECOND, CounterTick.INSTANCE);
    }

    void keepOrder(Order order) {
      long startTick = tickCount;
      if (processingState == ProcessingState.WAIT
          && processingOrder.getSortedDate() > order.getSortedDate()) {
        // replace processingOrder to our order
        Order o = processingOrder;
        processingOrder = order;
        order = o;
        startTick -= keepOrderBeforeSend.getSeconds() - tickNumberBeforeSend;
        setProcessingOrderTick();
      }

      WorkerOrder workerOrder = new WorkerOrder(order, startTick);
      if (doKeepInQueue) {
        ordersQueue.add(workerOrder);
        if (counterAnalyzer.incAndCheckForExceedingThreshold()) {
          startSavingInBuffer();
        }
      } else if (lastInQueue.getSortedDate() <= workerOrder.getSortedDate()) {
        initSavingBuffer();
        savingBuffer.add(workerOrder);
        if (!isBlockedByGet && savingBuffer.size() >= SAVING_BUFFER_MAX_SIZE) {
          asyncSaveOrdersInStorage();
        }
      } else if (processingState != ProcessingState.WAIT_FOR_GET) {
        ordersQueue.add(workerOrder);
        counterAnalyzer.inc();
      } else {
        waitOrSendOrder(workerOrder);
      }
    }

    void tick() {
      tickCount++;
      if (processingState == ProcessingState.WAIT && --tickNumberBeforeSend <= 0) {
        sendToCoordinator();
      }
      if (counterAnalyzer.tickAndCheckForExceedingThreshold()) {
        if (doKeepInQueue) {
          startSavingInBuffer();
        }
      } else if (!doKeepInQueue) {
        tryMovingOrdersToQueueFromBuffer();
      }
    }

    void initSavingBuffer() {
      if (savingBuffer == null) {
        savingBuffer = new ArrayList<>(SAVING_BUFFER_MAX_SIZE);
      }
    }

    void setProcessingOrderTick() {
      tickNumberBeforeSend = keepOrderBeforeSend.getSeconds();
    }

    void handleSavedOrdersNotification(SavedOrdersNotification notification) {
      numberOfSavedInStorage += notification.ordersNumber;
      if (--saveInStorageCallsNumber == 0 && needToGet) {
        needToGet = false;
        asyncGetOrdersFromStorage();
      }
    }

    void handleGetOrdersResponse(GetOrdersResponse response) {
      final List<WorkerOrder> bunch = response.bunch;
      final WorkerOrder lastOrder = bunch.get(bunch.size() - 1);
      isBlockedByGet = false;
      numberOfSavedInStorage -= bunch.size();
      for (Iterator<WorkerOrder> it = savingBuffer.iterator(); it.hasNext(); ) {
        WorkerOrder order = it.next();
        if (order.getSortedDate() < lastOrder.getSortedDate()) {
          it.remove();
          bunch.add(order);
        }
      }
      ordersQueue.addAll(bunch);
      counterAnalyzer.add(bunch.size());
      lastInQueue = ordersQueue.last();
    }

    void resourceReleased() {
      if (processOrderFromQueue()) {
        if (!doKeepInQueue && !counterAnalyzer.checkForExceedingThreshold()) {
          tryMovingOrdersToQueueFromBuffer();
        }
      } else if (doKeepInQueue) {
        finishProcessing();
      } else if (!hasOrdersInStorage()) {
        if (savingBuffer.isEmpty()) {
          doKeepInQueue = true;
          finishProcessing();
        } else {
          moveOrdersToQueueFromBuffer();
          processOrderFromQueue();
        }
      } else {
        if (!isBlockedByGet) {
          requestOrdersFromStorage();
        }
        processingState = ProcessingState.WAIT_FOR_GET;
        processingOrder = null;
      }
    }

    private void tryMovingOrdersToQueueFromBuffer() {
      if (!hasOrdersInStorage()) {
        if (savingBuffer.isEmpty()) {
          doKeepInQueue = true;
        } else {
          moveOrdersToQueueFromBuffer();
        }
      } else if (!isBlockedByGet) {
        requestOrdersFromStorage();
      }
    }

    private void requestOrdersFromStorage() {
      isBlockedByGet = true;
      if (saveInStorageCallsNumber == 0) {
        asyncGetOrdersFromStorage();
      } else {
        needToGet = true;
      }
    }

    private void moveOrdersToQueueFromBuffer() {
      ordersQueue.addAll(savingBuffer);
      counterAnalyzer.add(savingBuffer.size());
      savingBuffer.clear();
      lastInQueue = ordersQueue.last();
    }

    private boolean hasOrdersInStorage() {
      return saveInStorageCallsNumber > 0 || numberOfSavedInStorage > 0;
    }

    private void startSavingInBuffer() {
      doKeepInQueue = false;
      lastInQueue = ordersQueue.last();
    }

    private boolean processOrderFromQueue() {
      WorkerOrder workerOrder = ordersQueue.pollFirst();
      if (workerOrder != null) {
        counterAnalyzer.dec();
        waitOrSendOrder(workerOrder);
        return true;
      }
      return false;
    }

    private void waitOrSendOrder(WorkerOrder workerOrder) {
      processingOrder = workerOrder.getOrder();
      if (keepOrderBeforeSend != null) {
        tickNumberBeforeSend =
            keepOrderBeforeSend.getSeconds() - tickCount + workerOrder.getStartTick();
        if (tickNumberBeforeSend > 0) {
          processingState = ProcessingState.WAIT;
          return;
        }
      }
      sendToCoordinator();
    }

    private void asyncSaveOrdersInStorage() {
      saveInStorageCallsNumber++;
      final List<WorkerOrder> savingOrders = savingBuffer;
      savingBuffer = null;
      asyncCall(() -> {
        storage.saveWorkerOrders(sortedParameterKey, savingOrders);
        sendToItself(new SavedOrdersNotification(savingOrders.size()));
        return null;
      });
    }

    private void asyncGetOrdersFromStorage() {
      asyncCall(() -> {
        final List<WorkerOrder> bunch = storage
            .getWorkerOrders(sortedParameterKey, SAVING_BUFFER_MAX_SIZE);
        sendToItself(new GetOrdersResponse(bunch));
        return null;
      });
    }
  }

  enum ProcessingState {
    FREE,
    WAIT,
    SENT,
    WAIT_FOR_GET
  }

  static class CounterTick implements Serializable {

    static final CounterTick INSTANCE = new CounterTick();
  }

  static class KeepOrderTick implements Serializable {

    static final KeepOrderTick INSTANCE = new KeepOrderTick();
  }

  static class SavedOrdersNotification implements Serializable {

    long ordersNumber;

    SavedOrdersNotification(long ordersNumber) {
      this.ordersNumber = ordersNumber;
    }
  }

  static class GetOrdersResponse implements Serializable {

    List<WorkerOrder> bunch;

    GetOrdersResponse(List<WorkerOrder> bunch) {
      this.bunch = bunch;
    }
  }

}

