package fabuco.impl.storage;

import fabuco.impl.consumer.data.ConsumerBunch;
import fabuco.impl.consumer.data.WorkerOrder;
import fabuco.impl.executor.FabucoProcessError;
import fabuco.impl.executor.data.CoordinatorDataWrapper;
import java.time.LocalDateTime;
import java.util.List;

public interface FabucoStorage {

  /**
   * Every node has a number of key sets which can be changed dynamically if a new node is added
   * or removed from cluster.
   */
  List<String> getKeySetIdsByNode(String nodeId);

  /**
   * Node catches an order and retrieves its data before processing one.
   */
  OrderData catchOrder(String orderId);

  /**
   * Get orders for a certain keySetId sorted by sortedGroup with order date before lastTime.
   */
  ConsumerBunch getOrderBunch(String keySetId, long lastTime);

  /**
   * Save worker orders for certain sorted parameter key.
   */
  void saveWorkerOrders(SortedParameterKey key, List<WorkerOrder> orders);

  /**
   * Get worker orders by certain sorted parameter key.
   */
  List<WorkerOrder> getWorkerOrders(SortedParameterKey key, int count);

  void saveCoordinatorOrders(String attrCode, List<CoordinatorDataWrapper> orders);

  /**
   * Get orders sorted by a key
   */
  List<CoordinatorDataWrapper> getCoordinatorOrders(String attrCode, int count);

  void saveProcessCompleted(String resultAsJson, String orderId);

  void saveProcessFailed(FabucoProcessError errorInfo, String orderId);

  SubprocessData startSubprocess(String paramAsJson, String root, String parentId,
      String paramTypeCode, String childIndex, LocalDateTime expired);

  void saveSubProcessCompleted(String resultAsJson, String root, String parentId,
      String paramTypeCode, String childIndex);

  void saveSubProcessFailed(FabucoProcessError errorInfo, String root, String parentId,
      String paramTypeCode, String childIndex);

  PerformerData startPerformer(String paramAsJson, String root, String parentId,
      String paramTypeCode, String childIndex, LocalDateTime expired);

  void savePerformerCompleted(String resultAsJson, String root, String parentId,
      String paramTypeCode, String childIndex);

  void savePerformerFailed(String faultCode, Throwable recoverableError, String root,
      String parentId, String paramTypeCode, String childIndex);
}
