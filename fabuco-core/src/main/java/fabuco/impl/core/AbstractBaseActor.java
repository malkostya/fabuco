package fabuco.impl.core;

import static akka.actor.SupervisorStrategy.escalate;

import akka.actor.ActorRef;
import akka.actor.OneForOneStrategy;
import akka.actor.Scheduler;
import akka.actor.SupervisorStrategy;
import akka.actor.UntypedAbstractActor;
import akka.dispatch.Futures;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import scala.concurrent.Future;
import scala.concurrent.duration.FiniteDuration;

public abstract class AbstractBaseActor extends UntypedAbstractActor {

  protected static final ObjectMapper objectMapper = new ObjectMapper();
  private SupervisorStrategy strategy = getSupervisorStrategy();
  private final Scheduler scheduler = getContext().system().scheduler();

  @Override
  public final SupervisorStrategy supervisorStrategy() {
    return strategy;
  }

  protected SupervisorStrategy getSupervisorStrategy() {
    return new OneForOneStrategy(0, scala.concurrent.duration.Duration
        .create(1, TimeUnit.SECONDS), t -> escalate());
  }

  protected void sendToItself(Duration initialDelay, Duration interval, Object message) {
    scheduler.schedule(new FiniteDuration(initialDelay.toMillis(), TimeUnit.MILLISECONDS),
        scala.concurrent.duration.Duration.create(interval.toMillis(), TimeUnit.MILLISECONDS),
        getSelf(), message, getContext().dispatcher(), null);
  }

  protected void sendToItself(Duration delay, Object message) {
    scheduler.scheduleOnce(
        scala.concurrent.duration.Duration.create(delay.toMillis(), TimeUnit.MILLISECONDS),
        getSelf(), message, getContext().dispatcher(), null);
  }

  protected void sendToItself(Object message) {
    getSelf().tell(message, ActorRef.noSender());
  }

  protected void sendTo(ActorRef actor, Object message) {
    actor.tell(message, getSelf());
  }

  protected void sendTo(ActorRef actor, Duration delay, Object message) {
    scheduler.scheduleOnce(
        scala.concurrent.duration.Duration.create(delay.toMillis(), TimeUnit.MILLISECONDS),
        actor, message, getContext().dispatcher(), null);
  }

  protected Future asyncCall(Callable callable) {
    return Futures.future(callable, getContext().dispatcher());
  }

  protected <T> String valueToString(T value) {
    try {
      return objectMapper.writeValueAsString(value);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected <T> T stringToValue(String valueAsString, Class<T> valueType) {
    try {
      return objectMapper.readValue(valueAsString, valueType);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
