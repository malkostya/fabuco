package fabuco.impl.executor;

import akka.actor.ActorRef;
import fabuco.impl.exception.InvalidProcessException;
import fabuco.impl.executor.data.CallPerformerCommand;
import fabuco.impl.executor.data.ChildAddress;
import fabuco.impl.executor.data.PerformerAttributes;
import fabuco.impl.executor.data.PerformerCompleteResponse;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.data.StartSubProcessCommand;
import fabuco.impl.executor.data.SubProcessCompleteResponse;
import fabuco.impl.executor.data.SubProcessFailResponse;
import fabuco.impl.storage.PerformerData;
import fabuco.impl.storage.SubprocessData;
import fabuco.performer.PerformerCall;
import fabuco.performer.PerformerInterceptor;
import fabuco.performer.PerformerInterceptors;
import fabuco.performer.PerformerParameter;
import fabuco.performer.RetryAlgorithm;
import fabuco.process.ProcessCall;
import fabuco.process.ProcessParameter;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class ProcessExecutorInvoker<P extends ProcessParameter<R>, R> extends
    ProcessExecutorBase<P, R> {

  private Map<Class, Integer> indexes = new HashMap<>();
  private Map<ChildAddress, FabucoProcessCall> processCalls = new HashMap<>();
  private Map<ChildAddress, FabucoPerformerCall> performerCalls = new HashMap<>();
  private PerformerInterceptor defaultPerformerCallInterceptor =
      PerformerInterceptors.RETRY_IF_ERROR;
  private RetryAlgorithm defaultRetryAlgorithm = DefaultRetryAlgorithm.INSTANCE;

  public ProcessExecutorInvoker(ProcessExecutorContext context,
      ProcessAttributes<P, R> processAttributes,
      ActorRef invoker) {
    super(context, processAttributes, invoker);
  }

  @Override
  protected void onReceiveMessage(Object message) throws Exception {
    if (message instanceof SubProcessCompleteResponse) {
      handleSubProcessCompleteMessage((SubProcessCompleteResponse) message);
    } else if (message instanceof SubProcessFailResponse) {
      handleSubProcessFailMessage((SubProcessFailResponse) message);
    } else if (message instanceof PerformerCompleteResponse) {
      handlePerformerCompleteMessage((PerformerCompleteResponse) message);
    } else {
      super.onReceiveMessage(message);
    }
  }

  protected abstract void nextStep();

  private void checkForGoToNextStep() {
    numberOfActiveChildren--;
    if (isStepComplete()) {
      nextStep();
    }
  }

  private void handlePerformerCompleteMessage(PerformerCompleteResponse response) {
    performerCalls.put(response.getPerformerAddress(), response.getCall());
    checkForGoToNextStep();
  }

  private void handleSubProcessCompleteMessage(SubProcessCompleteResponse message) {
    FabucoProcessCall call = processCalls.get(message.getSubProcessAddress());
    call.setCompleted(message.getResult());
    checkForGoToNextStep();
  }

  private void handleSubProcessFailMessage(SubProcessFailResponse message) {
    FabucoProcessCall call = processCalls.get(message.getSubProcessAddress());
    call.setFailed(message.getErrorInfo());
    checkForGoToNextStep();
  }

  @Override
  public void setDefaultPerformerCallInterceptor(
      PerformerInterceptor defaultPerformerCallInterceptor) {
    this.defaultPerformerCallInterceptor = defaultPerformerCallInterceptor;
  }

  @Override
  public void setDefaultRetryAlgorithm(RetryAlgorithm defaultRetryAlgorithm) {
    this.defaultRetryAlgorithm = defaultRetryAlgorithm;
  }

  @Override
  public <T extends PerformerParameter<K>, K> String perform(T parameter) {
    return perform(parameter, null, null, true,
        defaultPerformerCallInterceptor);
  }

  @Override
  public <T extends PerformerParameter<K>, K> String perform(T parameter, String index) {
    return perform(parameter, index, null, true,
        defaultPerformerCallInterceptor);
  }

  @Override
  public <T extends PerformerParameter<K>, K> String perform(T parameter, String index,
      Duration lifetime) {
    return perform(parameter, index, lifetime, true,
        defaultPerformerCallInterceptor);
  }

  @Override
  public <T extends PerformerParameter<K>, K> String perform(T parameter,
      PerformerInterceptor<T, K> interceptor) {
    return perform(parameter, null, null, true, interceptor);
  }

  @Override
  public <T extends PerformerParameter<K>, K> String perform(T parameter, String index,
      PerformerInterceptor<T, K> interceptor) {
    return perform(parameter, index, null, true, interceptor);
  }

  @Override
  public <T extends PerformerParameter<K>, K> String perform(T parameter, String index,
      Duration lifetime, PerformerInterceptor<T, K> interceptor) {
    return perform(parameter, index, lifetime, true, interceptor);
  }

  private <T extends PerformerParameter<K>, K> String perform(final T parameter,
      String index, Duration lifetime, boolean save,
      final PerformerInterceptor<T, K> interceptor) {
    numberOfActiveChildren++;

    LocalDateTime expired = orderExpiredDate;
    if (lifetime == null) {
      lifetime = Duration
          .ofMillis(ChronoUnit.MILLIS.between(orderExpiredDate, LocalDateTime.now()));
    } else {
      expired = LocalDateTime.now().plus(lifetime.toMillis(), ChronoUnit.MILLIS);
    }

    if (index == null) {
      index = calcIndex(parameter.getClass());
    }

    final PerformerAttributes<?, K> attrs = context.getPerformerAttributes(parameter.getClass());
    final ChildAddress<T> address = new ChildAddress(parameter.getClass(), index);
    final FabucoPerformerCall<T, K> call = new FabucoPerformerCall<>(parameter);
    final PerformerData performerData = storage.startPerformer(valueToString(parameter), root,
        orderId, attrs.getCode(), index, expired);
    if (!performerData.hasResult()) {
      performerCalls.put(address, null);
      sendTo(attrs.getCoordinatorRef(), new CallPerformerCommand<>(call, orderPriority, lifetime,
          address, interceptor, defaultRetryAlgorithm, root, orderId, attrs.getCode()));
    } else {
      if (performerData.getResultAsString() != null) {
        final K result = stringToValue(performerData.getResultAsString(), attrs.getResultType());
        call.setCompleted(result);
      } else {
        call.setFailed(performerData.getFaultCode());
        call.setRecoverableError(performerData.getRecoverableError());
      }
      performerCalls.put(address, call);
      checkForGoToNextStep();
    }

    return index;
  }

  @Override
  public <T extends PerformerParameter<K>, K> PerformerCall<T, K> getPerformerCall(
      Class<T> parameterType) {
    return getPerformerCall(parameterType, getCurrentIndex(parameterType));
  }

  @Override
  public <T extends PerformerParameter<K>, K> PerformerCall<T, K> getPerformerCall(
      Class<T> parameterType, String index) {
    if (index != null) {
      FabucoPerformerCall<T, K> call = performerCalls
          .get(new ChildAddress<>(parameterType, index));
      if (call != null) {
        return call;
      }
    }
    throw new InvalidProcessException(
        "Call the performer with the parameter " + parameterType.getName() + " not found");
  }

  @Override
  public <T extends ProcessParameter<K>, K> String process(T parameter) {
    return process(parameter, null, orderExpiredDate, true);
  }

  @Override
  public <T extends ProcessParameter<K>, K> String process(T parameter, String index) {
    return process(parameter, index, orderExpiredDate, true);
  }

  @Override
  public <T extends ProcessParameter<K>, K> String process(T parameter, String index,
      LocalDateTime expired) {
    return process(parameter, index, expired, true);
  }

  private <T extends ProcessParameter<K>, K> String process(T parameter, String index,
      LocalDateTime expired, boolean save) {
    numberOfActiveChildren++;

    if (expired == null) {
      expired = orderExpiredDate;
    }

    if (index == null) {
      index = calcIndex(parameter.getClass());
    }

    final ProcessAttributes<?, K> attrs = context.getProcessAttributes(parameter.getClass());
    final ChildAddress<T> address = new ChildAddress(parameter.getClass(), index);
    final FabucoProcessCall<T, K> call = new FabucoProcessCall<>(parameter);
    final SubprocessData subprocessData = storage.startSubprocess(valueToString(parameter), root,
        orderId, attrs.getCode(), index, expired);
    processCalls.put(address, call);
    if (!subprocessData.hasResult()) {
      sendTo(attrs.getCoordinatorRef(), new StartSubProcessCommand<>(parameter, orderPriority,
          address, root, orderId, subprocessData.getOrderId(), expired));
    } else {
      if (subprocessData.getResultAsString() != null) {
        final K result = stringToValue(subprocessData.getResultAsString(), attrs.getResultType());
        call.setCompleted(result);
      } else {
        call.setFailed(subprocessData.getErrorInfo());
      }
      checkForGoToNextStep();
    }

    return index;
  }

  @Override
  public <T extends ProcessParameter<K>, K> ProcessCall<T, K> getProcessCall(
      Class<T> parameterType) {
    return getProcessCall(parameterType, getCurrentIndex(parameterType));
  }

  @Override
  public <T extends ProcessParameter<K>, K> ProcessCall<T, K> getProcessCall(
      Class<T> parameterType, String index) {
    if (index != null) {
      FabucoProcessCall<T, K> call = processCalls
          .get(new ChildAddress<>(parameterType, index));
      if (call != null) {
        return call;
      }
    }
    throw new InvalidProcessException(
        "Call the process with the parameter " + parameterType.getName() + " not found");
  }

  private String calcIndex(Class parameterType) {
    Integer index = indexes.get(parameterType);
    index = index != null ? index + 1 : 0;
    indexes.put(parameterType, index);
    return String.valueOf(index);
  }

  private String getCurrentIndex(Class parameterType) {
    Integer index = indexes.get(parameterType);
    return index != null ? String.valueOf(index) : null;
  }
}
