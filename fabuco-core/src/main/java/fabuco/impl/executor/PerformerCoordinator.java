package fabuco.impl.executor;

import static java.util.Comparator.comparing;

import akka.actor.ActorRef;
import akka.dispatch.Futures;
import fabuco.impl.core.FabucoConstants;
import fabuco.impl.core.AbstractBaseActor;
import fabuco.impl.executor.data.CallPerformerCommand;
import fabuco.impl.executor.data.PerformerAttributes;
import fabuco.impl.executor.data.PerformerCompleteResponse;
import fabuco.impl.executor.data.PerformerCoordinatorInitCommand;
import fabuco.impl.executor.data.ResourceReleasedNotification;
import fabuco.impl.storage.FabucoStorage;
import fabuco.impl.util.PrioritiesHandler;
import fabuco.impl.util.counter.CounterTick;
import fabuco.performer.InterceptorResult;
import fabuco.performer.InterceptorResult.ResultType;
import fabuco.performer.PerformerParameter;
import fabuco.performer.RetryAlgorithm;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PerformerCoordinator<P extends PerformerParameter<R>, R> extends
    AbstractBaseActor {

  private static final int TICK_COEF = 10;
  private final int activeCallsMaxNumber;
  private final PrioritiesHandler prioritiesHandler = new PrioritiesHandler(10);
  private PerformerAttributes<P, R> attributes;
  private FabucoStorage storage;
  private TreeSet<DataWrapper<P, R>> callsQueue = new TreeSet<>(
      comparing(DataWrapper::getKey));
  private Deque<RetryDataWrapper<P, R>> retryQueue = new ArrayDeque<>();
  private Map<Long, TickItem> tickItems = new HashMap<>();
  private ProcessingState processingState = ProcessingState.FREE;
  private long lastGlobalIndex;
  private int activeCallsNumber = 0;
  private volatile long tickCount = 0;
  private long callsCount = 0;

  public PerformerCoordinator(int activeCallsMaxNumber) {
    this.activeCallsMaxNumber = activeCallsMaxNumber;
    sendToItself(FabucoConstants.HUNDRED_MILLIS, FabucoConstants.HUNDRED_MILLIS,
        CounterTick.INSTANCE);
  }

  @Override
  public void onReceive(Object message) {
    if (message instanceof CallPerformerCommand) {
      handleRequest((CallPerformerCommand<P, R>) message);
    } else if (message instanceof ResourceReleasedNotification) {
      resourceReleased();
    } else if (message instanceof RetryDataWrapper) {
      retry((RetryDataWrapper) message);
    } else if (message instanceof CounterTick) {
      tick();
    } else if (message instanceof PerformerCoordinatorInitCommand) {
      init((PerformerCoordinatorInitCommand) message);
    }
  }

  private void init(PerformerCoordinatorInitCommand initCommand) {
    this.attributes = initCommand.getAttributes();
    this.storage = initCommand.getStorage();
  }

  private void tick() {
    TickItem tickItem = tickItems.remove(tickCount);
    if (tickItem != null) {
      tickItem.tick();
    }
    tickCount++;
  }

  private void retry(RetryDataWrapper wrapper) {
    TickItem tickItem = getTickItem(wrapper.key);
    tickItem.addCheckToRetry(wrapper);
    resourceReleased();
  }

  private void handleRequest(CallPerformerCommand command) {
    final long expired = calcTickValue(command.getLifetime());
    if (processingState == ProcessingState.FREE) {
      callPerformer(command, expired, getSender());
      if (++activeCallsNumber == activeCallsMaxNumber) {
        processingState = ProcessingState.ADD_TO_QUEUE;
      }
    } else {
      DataWrapper<P, R> wrapper = newCoordinatorDataWrapper(command, expired, getSender());
      callsQueue.add(wrapper);
      TickItem tickItem = getTickItem(expired);
      tickItem.addCheckToExpire(wrapper);
    }
  }

  private TickItem getTickItem(long key) {
    TickItem tickItem = tickItems.get(key);
    if (tickItem == null) {
      tickItem = new TickItem();
      tickItems.put(key, tickItem);
    }
    return tickItem;
  }

  private DataWrapper<P, R> newCoordinatorDataWrapper(
      CallPerformerCommand<P, R> command, long expired, ActorRef invoker) {
    long index = prioritiesHandler.getIndex(command.getPriority(), lastGlobalIndex);
    return new DataWrapper<>(index, expired, command, invoker);
  }

  private void resourceReleased() {
    if (processingState == ProcessingState.FREE) {
      activeCallsNumber--;
    } else {
      if (processingState == ProcessingState.ADD_TO_RETRY_QUEUE) {
        if (pollFromRetryQueueAndCallPerformer()) {
          return;
        }
        processingState = ProcessingState.ADD_TO_QUEUE;
      }
      if (!pollFromQueueAndCallPerformer()) {
        activeCallsNumber--;
        processingState = ProcessingState.FREE;
      }
    }
  }

  private boolean pollFromRetryQueueAndCallPerformer() {
    RetryDataWrapper wrapper = retryQueue.pollFirst();
    if (wrapper != null) {
      callPerformer(wrapper);
      return true;
    }
    return false;
  }

  private boolean pollFromQueueAndCallPerformer() {
    DataWrapper<P, R> wrapper = callsQueue.pollFirst();
    if (wrapper != null) {
      lastGlobalIndex = wrapper.getKey();
      callPerformer(wrapper.command, wrapper.expired, wrapper.invoker);
      TickItem tickItem = tickItems.get(wrapper.expired);
      tickItem.removeCheckToExpire(wrapper);
      return true;
    }
    return false;
  }

  private void callPerformer(final CallPerformerCommand<P, R> command, final long expired,
      final ActorRef invoker) {
    Futures.future(() -> {
      final InterceptorResult interceptorResult = callPerformer(command);
      if (interceptorResult.getType() == ResultType.RETURN_RESULT) {
        replyToInvoker(invoker, command);
      } else {
        final RetryAlgorithm retryAlgorithm =
            interceptorResult.getType() == ResultType.RETRY_BY_DEFAULT_ALGORITHM
                ? command.getDefaultRetryAlgorithm()
                : interceptorResult.getRetryAlgorithm();
        final Duration nextRetry = retryAlgorithm.nextRetry(0);
        final long key = calcTickValue(nextRetry, expired);
        final RetryDataWrapper<P, R> wrapper = new RetryDataWrapper<>(key, expired,
            getNextCallsCount(), command, invoker, retryAlgorithm, 0);
        sendToItself(wrapper);
      }
      return null;
    }, getContext().dispatcher());
  }

  private void replyToInvoker(ActorRef invoker, CallPerformerCommand<P, R> command) {
    sendToItself(ResourceReleasedNotification.INSTANCE);
    sendTo(invoker, new PerformerCompleteResponse(command.getAddress(), command.getCall()));
  }

  private InterceptorResult callPerformer(final CallPerformerCommand<P, R> command) {
    attributes.getFunction().apply(attributes.getPerformer(), command.getCall());
    final FabucoPerformerCall<P, R> call = command.getCall();
    if (call.isSucceeded()) {
      storage.savePerformerCompleted(valueToString(call.getResult()), command.getRoot(),
          command.getParentId(), command.getParamTypeCode(), command.getAddress().getIndex());
    } else {
      storage.savePerformerFailed(call.getFaultCode(), call.getRecoverableError(),
          command.getRoot(), command.getParentId(), command.getParamTypeCode(),
          command.getAddress().getIndex());
    }
    return command.getInterceptor().apply(command.getCall());
  }

  private void callPerformer(final RetryDataWrapper wrapper) {
    Futures.future(() -> {
      final CallPerformerCommand<P, R> command = wrapper.command;
      final InterceptorResult interceptorResult = callPerformer(command);
      if (interceptorResult.getType() == ResultType.RETURN_RESULT) {
        replyToInvoker(wrapper.invoker, command);
      } else if (wrapper.isExpired()) {
        command.getCall().setExpired();
        replyToInvoker(wrapper.invoker, command);
      } else {
        final Duration nextRetry = wrapper.nextRetry();
        final long key = calcTickValue(nextRetry, wrapper.expired);
        wrapper.setKey(key);
        sendToItself(wrapper);
      }
      return null;
    }, getContext().dispatcher());
  }

  private long calcTickValue(Duration nextRetry) {
    return nextRetry.getSeconds() * TICK_COEF + tickCount;
  }

  private long calcTickValue(Duration nextRetry, long maxValue) {
    long value = calcTickValue(nextRetry);
    if (value > maxValue) {
      value = maxValue;
    }
    return value;
  }

  private long getNextCallsCount() {
    if (++callsCount == Long.MAX_VALUE) {
      callsCount = 0;
    }
    return callsCount;
  }

  enum ProcessingState {
    FREE,
    ADD_TO_QUEUE,
    ADD_TO_RETRY_QUEUE
  }

  class TickItem {

    List<RetryDataWrapper<P, R>> retryDataWrappers;
    Set<DataWrapper<P, R>> dataWrappers;

    void tick() {
      checkToRetry();
      checkToExpire();
    }

    void addCheckToRetry(RetryDataWrapper<P, R> wrapper) {
      if (retryDataWrappers == null) {
        retryDataWrappers = new ArrayList<>();
      }
      retryDataWrappers.add(wrapper);
    }

    void addCheckToExpire(DataWrapper<P, R> wrapper) {
      if (dataWrappers == null) {
        dataWrappers = new HashSet<>();
      }
      dataWrappers.add(wrapper);
    }

    void removeCheckToExpire(DataWrapper<P, R> wrapper) {
      dataWrappers.remove(wrapper);
    }

    void checkToRetry() {
      if (retryDataWrappers != null) {
        retryDataWrappers.stream().forEach(wrapper -> {
          if (processingState == ProcessingState.FREE) {
            callPerformer(wrapper);
            if (++activeCallsNumber == activeCallsMaxNumber) {
              processingState = ProcessingState.ADD_TO_QUEUE;
            }
          } else {
            if (processingState == ProcessingState.ADD_TO_QUEUE) {
              processingState = ProcessingState.ADD_TO_RETRY_QUEUE;
            }
            retryQueue.add(wrapper);
          }
        });
      }
    }

    void checkToExpire() {
      if (dataWrappers != null && !dataWrappers.isEmpty()) {
        dataWrappers.stream().forEach(wrapper -> {
          callsQueue.remove(wrapper);
          wrapper.command.getCall().setExpiredWithoutTry();
          replyToInvoker(wrapper.invoker, wrapper.command);
        });
      }
    }
  }

  static class RetryDataWrapper<P extends PerformerParameter<R>, R> implements Serializable {

    long key;
    final long expired;
    final long callsCount;
    final CallPerformerCommand<P, R> command;
    final ActorRef invoker;
    final RetryAlgorithm retryAlgorithm;
    int retryNumber = 0;

    RetryDataWrapper(long key, long expired, long callsCount,
        CallPerformerCommand<P, R> command, ActorRef invoker,
        RetryAlgorithm retryAlgorithm, int retryNumber) {
      this.key = key;
      this.expired = expired;
      this.callsCount = callsCount;
      this.command = command;
      this.invoker = invoker;
      this.retryAlgorithm = retryAlgorithm;
      this.retryNumber = retryNumber;
    }

    void setKey(long key) {
      this.key = key;
    }

    boolean isExpired() {
      return key == expired;
    }

    Duration nextRetry() {
      return retryAlgorithm.nextRetry(++retryNumber);
    }
  }

  static class DataWrapper<P extends PerformerParameter<R>, R> implements Serializable {

    final long key;
    final long expired;
    final CallPerformerCommand<P, R> command;
    final ActorRef invoker;

    DataWrapper(long key, long expired, CallPerformerCommand<P, R> command,
        ActorRef invoker) {
      this.key = key;
      this.expired = expired;
      this.command = command;
      this.invoker = invoker;
    }

    long getKey() {
      return key;
    }
  }

}
