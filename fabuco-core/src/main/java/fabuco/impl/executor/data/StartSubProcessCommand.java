package fabuco.impl.executor.data;

import fabuco.process.ProcessParameter;
import java.io.Serializable;
import java.time.LocalDateTime;
import lombok.Value;

@Value
public class StartSubProcessCommand<P extends ProcessParameter<R>, R> implements Serializable {

  private static final long serialVersionUID = 6674650926449093456L;
  private final P parameter;
  private final int priority;
  private final ChildAddress<P> address;
  private final String root;
  private final String parentId;
  private final String orderId;
  private final LocalDateTime expired;
}
