package fabuco.impl.executor;

import akka.actor.ActorRef;
import fabuco.impl.consumer.data.Order;
import fabuco.impl.core.AbstractBaseActor;
import fabuco.impl.exception.InvalidProcessException;
import fabuco.impl.executor.data.ChildAddress;
import fabuco.impl.executor.data.FabucoStepResult;
import fabuco.impl.executor.data.ProcessAttributes;
import fabuco.impl.executor.data.ResourceReleasedNotification;
import fabuco.impl.executor.data.StartSubProcessCommand;
import fabuco.impl.storage.FabucoStorage;
import fabuco.impl.storage.OrderData;
import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.process.ProcessParameter;
import fabuco.step.StepResult;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public abstract class ProcessExecutorBase<P extends ProcessParameter<R>, R>
    extends AbstractBaseActor implements ProcessExecutor<R> {

  protected static final StepResult FINAL_STEP = FabucoStepResult.INSTANCE;
  protected final FabucoStorage storage;
  protected final ProcessExecutorContext context;
  protected final ProcessAttributes<P, R> attributes;
  protected final ActorRef coordinator;
  protected final ActorRef invoker;
  protected boolean isRoot;
  protected FabucoProcess<P, R> process;
  protected P processParameter;
  protected String orderId;
  protected String root;
  protected String parentId;
  protected int orderPriority;
  protected LocalDateTime orderExpiredDate = LocalDateTime.now().plusSeconds(10000);
  protected ChildAddress childAddress;
  protected int numberOfActiveChildren = 0;

  private final boolean isPrimalExecuted;

  public ProcessExecutorBase(ProcessExecutorContext context, ProcessAttributes<P, R> attributes,
      ActorRef invoker) {
    this.attributes = attributes;
    this.context = context;
    this.invoker = invoker;
    this.storage = context.getStorage();
    this.coordinator = context().parent();
    createProcess(attributes.getProcessType());
    isPrimalExecuted = true;
  }

  protected void onReceiveMessage(Object message) throws Exception {
    if (message instanceof Order) {
      final Order<P, R> order = (Order<P, R>) message;
      isRoot = true;
      root = orderId = order.getOrderId();
      orderPriority = order.getPriority();
      orderExpiredDate = epochToLocalDateTime(order.getExpired());
      final OrderData orderData = storage.catchOrder(orderId);
      processParameter = stringToOrderParam(orderData.getParameterAsJson());
      callOnStart();
    } else if (message instanceof StartSubProcessCommand) {
      final StartSubProcessCommand<P, R> msg = (StartSubProcessCommand<P, R>) message;
      isRoot = false;
      root = msg.getRoot();
      parentId = msg.getParentId();
      orderId = msg.getOrderId();
      orderPriority = msg.getPriority();
      orderExpiredDate = msg.getExpired();
      processParameter = msg.getParameter();
      childAddress = msg.getAddress();
      callOnStart();
    } else {
      throw new RuntimeException("Message " + message + " is not found");
    }

  }

  private void createProcess(Class processType) {
    try {
      process = (FabucoProcess<P, R>) processType.newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new InvalidProcessException(
          "Default constructor is not found for " + processType.getName());
    }
  }

  private void callOnStart() {
    numberOfActiveChildren = 0;
    StepResult stepResult = process.onStart(this, processParameter);
    handleStepResult(stepResult);
  }

  protected abstract void handleStepResult(StepResult stepResult);

  protected boolean isStepComplete() {
    return numberOfActiveChildren == 0;
  }

  protected void sendToInvoker(Object message) {
    invoker.tell(message, getSelf());
  }

  protected void sendToCoordinator(Object message) {
    coordinator.tell(message, getSelf());
  }

  protected P stringToOrderParam(String paramAsString) {
    try {
      return objectMapper.readValue(paramAsString, attributes.getParameterType());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  protected LocalDateTime epochToLocalDateTime(long epochValue) {
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochValue), ZoneId.systemDefault());
  }

  protected void stopActor() {
    sendToCoordinator(ResourceReleasedNotification.INSTANCE);
    if (isRoot && attributes.inSortedGroup()) {
      sendToInvoker(ResourceReleasedNotification.INSTANCE);
    }
    getContext().stop(getSelf());
  }
}
