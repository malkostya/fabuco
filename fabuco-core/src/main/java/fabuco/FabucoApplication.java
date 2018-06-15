package fabuco;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import fabuco.impl.core.FabucoServer;
import fabuco.impl.core.FabucoServerCloseCommand;
import fabuco.impl.core.FabucoServerInitCommand;

public class FabucoApplication {

  private final ActorRef server;

  public FabucoApplication() {
    final ActorSystem system = ActorSystem.create("fabuco");
    server = system.actorOf(Props.create(FabucoServer.class));
    server.tell(FabucoServerInitCommand.INSTANCE, ActorRef.noSender());
  }

  public void close() {
    server.tell(FabucoServerCloseCommand.INSTANCE, ActorRef.noSender());
  }
}
