package fabuco.impl.executor.data;

import akka.actor.ActorRef;
import fabuco.process.FabucoProcess;
import fabuco.process.ProcessParameter;
import lombok.Value;

@Value
public class ProcessAttributes<P extends ProcessParameter<R>, R> {

  private final String code;
  private final Class<P> parameterType;
  private final Class<R> resultType;
  private final Class<? extends FabucoProcess<P, R>> processType;
  private final ActorRef coordinatorRef;
  private final String sortedGroup;
  private final String dispatcher;

  public boolean inSortedGroup() {
    return sortedGroup != null;
  }
}
