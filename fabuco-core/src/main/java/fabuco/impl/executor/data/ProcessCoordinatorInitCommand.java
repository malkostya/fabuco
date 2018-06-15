package fabuco.impl.executor.data;

import fabuco.impl.executor.ProcessExecutorContext;
import fabuco.process.ProcessParameter;
import java.io.Serializable;
import lombok.Value;

@Value
public class ProcessCoordinatorInitCommand<P extends ProcessParameter<R>, R> implements
    Serializable {

  private static final long serialVersionUID = -5250012758064748036L;
  private final ProcessAttributes<P, R> attributes;
  private final ProcessExecutorContext executorContext;
}
