package fabuco.impl.executor.data;

import akka.actor.ActorRef;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class CoordinatorDataWrapper<T> implements Serializable {

  private static final long serialVersionUID = -5640342206034547317L;
  private final long key;
  private final T data;
  private final ActorRef invoker;
}
