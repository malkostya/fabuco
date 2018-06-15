package fabuco.impl.executor;

import static java.util.Comparator.comparing;

import akka.actor.ActorRef;
import akka.actor.Props;
import fabuco.impl.core.FabucoConstants;
import fabuco.impl.consumer.data.Order;
import fabuco.impl.core.AbstractBaseActor;
import fabuco.impl.executor.data.CoordinatorDataWrapper;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.data.ProcessCoordinatorInitCommand;
import fabuco.impl.executor.data.ResourceReleasedNotification;
import fabuco.impl.executor.data.StartSubProcessCommand;
import fabuco.impl.storage.FabucoStorage;
import fabuco.impl.util.PrioritiesHandler;
import fabuco.impl.util.counter.LongCounterAnalyzer;
import fabuco.impl.util.counter.CounterTick;
import fabuco.process.ProcessParameter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

public class ProcessCoordinator<P extends ProcessParameter<R>, R> extends
    AbstractBaseActor {

  private static final int SAVING_BUFFER_MAX_SIZE = 100;
  private final int activeOrdersMaxNumber;
  private final PrioritiesHandler prioritiesHandler = new PrioritiesHandler(10);
  private final LongCounterAnalyzer counterAnalyzer;
  private ProcessAttributes<P, R> attributes;
  private ProcessExecutorContext executorContext;
  private FabucoStorage storage;
  private TreeSet<CoordinatorDataWrapper> ordersQueue = new TreeSet<>(
      comparing(CoordinatorDataWrapper::getKey));
  private ProcessingState processingState = ProcessingState.FREE;
  private OrdersKeeper keeper;
  private long lastGlobalIndex;
  private long lastIndexInQueue;
  private int activeOrdersNumber = 0;

  public ProcessCoordinator(int activeOrdersMaxNumber) {
    this.activeOrdersMaxNumber = activeOrdersMaxNumber;
    this.counterAnalyzer = new LongCounterAnalyzer((short) 10, activeOrdersMaxNumber * 10);
    sendToItself(FabucoConstants.ONE_SECOND, FabucoConstants.ONE_SECOND, CounterTick.INSTANCE);
  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof Order) {
      Order<P, R> order = (Order<P, R>) message;
      handleRequest(order, order.getPriority());
    } else if (message instanceof StartSubProcessCommand) {
      StartSubProcessCommand<P, R> command = (StartSubProcessCommand<P, R>) message;
      handleRequest(command, 0);
    } else if (message instanceof ResourceReleasedNotification) {
      resourceReleased();
    } else if (message instanceof CounterTick) {
      tick();
    } else if (message instanceof SavedOrdersNotification) {
      keeper.handleSavedOrdersNotification((SavedOrdersNotification) message);
    } else if (message instanceof GetOrdersResponse) {
      keeper.handleGetOrdersResponse((GetOrdersResponse) message);
    } else if (message instanceof ProcessCoordinatorInitCommand) {
      init((ProcessCoordinatorInitCommand) message);
    }
  }

  private void init(ProcessCoordinatorInitCommand initCommand) {
    this.attributes = initCommand.getAttributes();
    this.executorContext = initCommand.getExecutorContext();
    this.storage = executorContext.getStorage();
  }

  private void tick() {
    if (counterAnalyzer.tickAndCheckForExceedingThreshold()) {
      if (processingState == ProcessingState.ADD_TO_QUEUE) {
        startSavingInBuffer();
      }
    } else if (processingState == ProcessingState.ADD_TO_BUFFER) {
      if (keeper.tryPopulatingQueue() != KeeperResult.REQUEST_STORAGE) {
        processingState = ProcessingState.ADD_TO_QUEUE;
      }
    }
  }

  private void handleRequest(Object data, int priority) {
    switch (processingState) {
      case FREE:
        sendMessageToExecutor(data, getSender());
        counterAnalyzer.inc();
        if (++activeOrdersNumber == activeOrdersMaxNumber) {
          processingState = ProcessingState.ADD_TO_QUEUE;
        }
        break;
      case ADD_TO_QUEUE: {
        CoordinatorDataWrapper dataWrapper = newCoordinatorDataWrapper(data, priority, getSender());
        ordersQueue.add(dataWrapper);
        if (counterAnalyzer.incAndCheckForExceedingThreshold()) {
          startSavingInBuffer();
        }
        break;
      }
      case ADD_TO_BUFFER: {
        CoordinatorDataWrapper dataWrapper = newCoordinatorDataWrapper(data, priority, getSender());
        if (dataWrapper.getKey() < lastIndexInQueue) {
          ordersQueue.add(dataWrapper);
          counterAnalyzer.inc();
        } else {
          keeper.addToBuffer(dataWrapper);
        }
        break;
      }
      default: // WAIT_FOR_GET
        long index = prioritiesHandler.getIndex(priority, lastGlobalIndex);
        if (index < lastIndexInQueue) {
          if (activeOrdersNumber < activeOrdersMaxNumber) {
            sendMessageToExecutor(data, getSender());
            counterAnalyzer.inc();
            activeOrdersNumber++;
          } else {
            CoordinatorDataWrapper dataWrapper = new CoordinatorDataWrapper(index, data,
                getSender());
            ordersQueue.add(dataWrapper);
            counterAnalyzer.inc();
            processingState = ProcessingState.ADD_TO_BUFFER;
          }
        } else {
          CoordinatorDataWrapper dataWrapper = new CoordinatorDataWrapper(index, data, getSender());
          keeper.addToBuffer(dataWrapper);
        }
    }
  }

  private void startSavingInBuffer() {
    processingState = ProcessingState.ADD_TO_BUFFER;
    lastIndexInQueue = ordersQueue.last().getKey();
    if (keeper == null) {
      keeper = new OrdersKeeper();
    }
  }

  private CoordinatorDataWrapper newCoordinatorDataWrapper(Object data, int priority,
      ActorRef invoker) {
    long index = prioritiesHandler.getIndex(priority, lastGlobalIndex);
    return new CoordinatorDataWrapper(index, data, invoker);
  }

  private void resourceReleased() {
    counterAnalyzer.dec();
    switch (processingState) {
      case FREE:
      case WAIT_FOR_GET:
        activeOrdersNumber--;
        break;
      case ADD_TO_QUEUE: {
        if (!pollFromQueueAndSendMessageToExecutor()) {
          activeOrdersNumber--;
          processingState = ProcessingState.FREE;
        }
        break;
      }
      default: { // ADD_TO_BUFFER
        if (pollFromQueueAndSendMessageToExecutor()) {
          break;
        }
        switch (keeper.tryPopulatingQueue()) {
          case MOVED_TO_QUEUE_AND_NO_ORDERS:
            pollFromQueueAndSendMessageToExecutor();
            processingState = ProcessingState.ADD_TO_QUEUE;
            break;
          case NO_ORDERS:
            activeOrdersNumber--;
            processingState = ProcessingState.FREE;
            break;
          default: // REQUEST_STORAGE
            activeOrdersNumber--;
            processingState = ProcessingState.WAIT_FOR_GET;
        }
      }
    }
  }

  private boolean pollFromQueueAndSendMessageToExecutor() {
    CoordinatorDataWrapper dataWrapper = ordersQueue.pollFirst();
    if (dataWrapper != null) {
      lastGlobalIndex = dataWrapper.getKey();
      sendMessageToExecutor(dataWrapper.getData(), dataWrapper.getInvoker());
      return true;
    }
    return false;
  }

  private void sendMessageToExecutor(Object message, ActorRef invoker) {
    final ActorRef executor = getContext().actorOf(
        Props.create(FabucoProcessExecutor.class, executorContext, attributes, invoker)
            .withDispatcher(attributes.getDispatcher()));
    executor.tell(message, getSelf());
  }

  static class SavedOrdersNotification implements Serializable {

    long ordersNumber;

    SavedOrdersNotification(long ordersNumber) {
      this.ordersNumber = ordersNumber;
    }
  }

  static class GetOrdersResponse implements Serializable {

    List<CoordinatorDataWrapper> bunch;

    GetOrdersResponse(List<CoordinatorDataWrapper> bunch) {
      this.bunch = bunch;
    }
  }

  enum ProcessingState {
    FREE,
    ADD_TO_QUEUE,
    ADD_TO_BUFFER,
    WAIT_FOR_GET
  }

  enum KeeperResult {
    NO_ORDERS,
    MOVED_TO_QUEUE_AND_NO_ORDERS,
    REQUEST_STORAGE
  }

  class OrdersKeeper {

    List<CoordinatorDataWrapper> savingBuffer = new ArrayList<>(SAVING_BUFFER_MAX_SIZE);
    int saveInStorageCallsNumber = 0;
    long numberOfSavedInStorage = 0;
    boolean isBlockedByGet = false;
    boolean needToGet = false;

    void addToBuffer(CoordinatorDataWrapper coordinatorOrder) {
      savingBuffer.add(coordinatorOrder);
      if (!isBlockedByGet && savingBuffer.size() >= SAVING_BUFFER_MAX_SIZE) {
        asyncSaveOrdersInStorage();
      }
    }

    KeeperResult tryPopulatingQueue() {
      if (!hasOrdersInStorage()) {
        return tryMovingOrdersToQueueFromBuffer();
      } else {
        requestOrdersFromStorage();
        return KeeperResult.REQUEST_STORAGE;
      }
    }

    KeeperResult tryMovingOrdersToQueueFromBuffer() {
      if (!savingBuffer.isEmpty()) {
        moveOrdersToQueueFromBuffer();
        return KeeperResult.MOVED_TO_QUEUE_AND_NO_ORDERS;
      }
      return KeeperResult.NO_ORDERS;
    }

    void handleSavedOrdersNotification(SavedOrdersNotification notification) {
      numberOfSavedInStorage += notification.ordersNumber;
      if (--saveInStorageCallsNumber == 0 && needToGet) {
        needToGet = false;
        asyncGetOrdersFromStorage();
      }
    }

    void handleGetOrdersResponse(GetOrdersResponse response) {
      final List<CoordinatorDataWrapper> bunch = response.bunch;
      final CoordinatorDataWrapper lastOrder = bunch.get(bunch.size() - 1);
      isBlockedByGet = false;
      numberOfSavedInStorage -= bunch.size();
      for (Iterator<CoordinatorDataWrapper> it = savingBuffer.iterator(); it.hasNext(); ) {
        CoordinatorDataWrapper coordinatorOrder = it.next();
        if (coordinatorOrder.getKey() < lastOrder.getKey()) {
          it.remove();
          bunch.add(coordinatorOrder);
        }
      }
      ordersQueue.addAll(bunch);
      counterAnalyzer.add(bunch.size());
      lastIndexInQueue = ordersQueue.last().getKey();
    }

    void moveOrdersToQueueFromBuffer() {
      ordersQueue.addAll(savingBuffer);
      counterAnalyzer.add(savingBuffer.size());
      savingBuffer.clear();
      lastIndexInQueue = ordersQueue.last().getKey();
    }

    boolean hasOrdersInStorage() {
      return saveInStorageCallsNumber > 0 || numberOfSavedInStorage > 0;
    }

    void requestOrdersFromStorage() {
      if (!isBlockedByGet) {
        isBlockedByGet = true;
        if (saveInStorageCallsNumber == 0) {
          asyncGetOrdersFromStorage();
        } else {
          needToGet = true;
        }
      }
    }

    void asyncSaveOrdersInStorage() {
      saveInStorageCallsNumber++;
      final List<CoordinatorDataWrapper> savingOrders = savingBuffer;
      savingBuffer = new ArrayList<>(SAVING_BUFFER_MAX_SIZE);
      asyncCall(() -> {
        storage.saveCoordinatorOrders(attributes.getCode(), savingOrders);
        sendToItself(new SavedOrdersNotification(savingOrders.size()));
        return null;
      });
    }

    void asyncGetOrdersFromStorage() {
      asyncCall(() -> {
        final List<CoordinatorDataWrapper> bunch = storage
            .getCoordinatorOrders(attributes.getCode(), SAVING_BUFFER_MAX_SIZE);
        sendToItself(new GetOrdersResponse(bunch));
        return null;
      });
    }
  }
}
