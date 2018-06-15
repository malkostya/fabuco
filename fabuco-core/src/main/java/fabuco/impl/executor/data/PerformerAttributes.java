package fabuco.impl.executor.data;

import akka.actor.ActorRef;
import fabuco.impl.executor.PerformerFunction;
import fabuco.performer.PerformerCallContext;
import fabuco.performer.PerformerParameter;
import lombok.Value;

@Value
public class PerformerAttributes<P extends PerformerParameter<R>, R> {

  private final String code;
  private final Class<P> parameterType;
  private final Class<R> resultType;
  private final Object performer;
  private final PerformerFunction<Object, PerformerCallContext<P, R>> function;
  private final ActorRef coordinatorRef;
  private final String dispatcher;
}
