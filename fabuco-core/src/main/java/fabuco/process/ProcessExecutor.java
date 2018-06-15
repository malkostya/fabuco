package fabuco.process;

import fabuco.performer.PerformerCall;
import fabuco.performer.PerformerInterceptor;
import fabuco.performer.PerformerParameter;
import fabuco.performer.RetryAlgorithm;
import fabuco.process.ProcessCall;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.time.Duration;
import java.time.LocalDateTime;

public interface ProcessExecutor<R> {

  StepResult stepTo(String methodName);

  StepResult stepTo(String methodName, Object... methodArgs);

  void setDefaultPerformerCallInterceptor(
      PerformerInterceptor defaultPerformerCallInterceptor);

  void setDefaultRetryAlgorithm(RetryAlgorithm defaultRetryAlgorithm);

  <T extends ProcessParameter<K>, K> String process(T parameter);

  <T extends ProcessParameter<K>, K> String process(T parameter, String index);

  <T extends ProcessParameter<K>, K> String process(T parameter, String index,
      LocalDateTime expired);

  <T extends ProcessParameter<K>, K> ProcessCall<T, K> getProcessCall(
      Class<T> parameterType);

  <T extends ProcessParameter<K>, K> ProcessCall<T, K> getProcessCall(
      Class<T> parameterType, String index);

  <T extends PerformerParameter<K>, K> String perform(T parameter);

  <T extends PerformerParameter<K>, K> String perform(T parameter, String index);

  <T extends PerformerParameter<K>, K> String perform(T parameter, String index,
      Duration lifetime);

  <T extends PerformerParameter<K>, K> String perform(T parameter,
      PerformerInterceptor<T, K> validator);

  <T extends PerformerParameter<K>, K> String perform(T parameter, String index,
      PerformerInterceptor<T, K> validator);

  <T extends PerformerParameter<K>, K> String perform(T parameter, String index,
      Duration lifetime, PerformerInterceptor<T, K> validator);

  <T extends PerformerParameter<K>, K> PerformerCall<T, K> getPerformerCall(
      Class<T> parameterType);

  <T extends PerformerParameter<K>, K> PerformerCall<T, K> getPerformerCall(
      Class<T> parameterType, String index);

  StepResult complete(R result);

  StepResult fail(String errorMessage);

}
