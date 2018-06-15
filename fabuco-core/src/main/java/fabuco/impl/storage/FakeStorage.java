package fabuco.impl.storage;

import static java.util.Comparator.comparing;

import fabuco.impl.consumer.data.ConsumerBunch;
import fabuco.impl.consumer.data.Order;
import fabuco.impl.consumer.data.WorkerOrder;
import fabuco.impl.executor.FabucoProcessError;
import fabuco.impl.executor.data.CoordinatorDataWrapper;
import fabuco.process.ParameterKey;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;

/**
 * The temporary class serves to emulate FabucoStorage and is going to be substitute for real
 * fabuco storage in future.
 * The class is closely linked to fabuco.examples.camel.Persons because fabuco client designed
 * for creating fabuco processes has not implemented yet.
 */
@AllArgsConstructor
public class FakeStorage implements FabucoStorage {

  private final Map<SortedParameterKey, List<WorkerOrder>> workerOrders = new HashMap<>();
  private final Map<String, List<CoordinatorDataWrapper>> coordinatorOrders = new HashMap<>();

  @Override
  public List<String> getKeySetIdsByNode(String nodeId) {
    return Arrays.asList("key set 1", "key set 2");
  }

  @Override
  public OrderData catchOrder(String orderId) {
    return new OrderData(
        "{\"namesListUniqueId\": 1, \"names\": [\"John\", \"Mary\", \"Harry\"]}");
  }

  @Override
  public ConsumerBunch getOrderBunch(String keySetId, long lastTime) {
    if (lastTime == 0 && keySetId.equals("key set 1")) {
      ConsumerBunch bunch = new ConsumerBunch(1, Arrays.asList(
          new Order("my order id", 5, System.currentTimeMillis() + 600000,
              1, new ParameterKey("NAMES LIST UNIQUE ID", "1"),
              "greeting manager process code")
      ));
      return bunch;
    }
    return null;
  }

  @Override
  public void saveWorkerOrders(SortedParameterKey key, List<WorkerOrder> orders) {
    orders.sort(comparing(WorkerOrder::getSortedDate));
    workerOrders.put(key, orders);
  }

  @Override
  public List<WorkerOrder> getWorkerOrders(SortedParameterKey key, int count) {
    List<WorkerOrder> orders = workerOrders.get(key);
    return orders.size() > count ? orders.subList(0, count) : orders;
  }

  @Override
  public void saveCoordinatorOrders(String attrCode, List<CoordinatorDataWrapper> orders) {
    orders.sort(comparing(CoordinatorDataWrapper::getKey));
    coordinatorOrders.put(attrCode, orders);
  }

  @Override
  public List<CoordinatorDataWrapper> getCoordinatorOrders(String attrCode, int count) {
    List<CoordinatorDataWrapper> orders = coordinatorOrders.get(attrCode);
    return orders.size() > count ? orders.subList(0, count) : orders;

  }

  @Override
  public void saveProcessCompleted(String resultAsJson, String orderId) {

  }

  @Override
  public void saveProcessFailed(FabucoProcessError errorInfo, String orderId) {

  }

  @Override
  public SubprocessData startSubprocess(String paramAsJson, String root, String parentId,
      String paramTypeCode, String childIndex, LocalDateTime expired) {
    return new SubprocessData("my subprocess orderId");
  }

  @Override
  public void saveSubProcessCompleted(String resultAsJson, String root, String parentId,
      String paramTypeCode, String childIndex) {

  }

  @Override
  public void saveSubProcessFailed(FabucoProcessError errorInfo, String root, String parentId,
      String paramTypeCode, String childIndex) {

  }

  @Override
  public PerformerData startPerformer(String paramAsJson, String root, String parentId,
      String paramTypeCode, String childIndex, LocalDateTime expired) {
    return new PerformerData("my performer orderId");
  }

  @Override
  public void savePerformerCompleted(String resultAsJson, String root, String parentId,
      String paramTypeCode, String childIndex) {

  }

  @Override
  public void savePerformerFailed(String faultCode, Throwable recoverableError, String root,
      String parentId, String paramTypeCode, String childIndex) {

  }
}
