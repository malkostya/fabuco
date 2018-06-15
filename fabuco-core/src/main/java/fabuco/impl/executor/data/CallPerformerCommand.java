package fabuco.impl.executor.data;

import fabuco.impl.executor.FabucoPerformerCall;
import fabuco.performer.PerformerInterceptor;
import fabuco.performer.PerformerParameter;
import fabuco.performer.RetryAlgorithm;
import java.io.Serializable;
import java.time.Duration;
import lombok.Value;

@Value
public class CallPerformerCommand<P extends PerformerParameter<R>, R> implements Serializable {

  private static final long serialVersionUID = 8542433758136506929L;
  private final FabucoPerformerCall<P, R> call;
  private final int priority;
  private final Duration lifetime;
  private final ChildAddress<P> address;
  private final PerformerInterceptor<P, R> interceptor;
  private final RetryAlgorithm defaultRetryAlgorithm;
  private final String root;
  private final String parentId;
  private final String paramTypeCode;
}
