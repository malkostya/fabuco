package fabuco.impl.executor.data;

import fabuco.impl.executor.FabucoProcessError;
import fabuco.process.ProcessParameter;
import java.io.Serializable;
import lombok.Value;

@Value
public class SubProcessFailResponse<P extends ProcessParameter<R>, R> implements Serializable {

  private static final long serialVersionUID = -7462745976882284130L;
  private final ChildAddress<P> subProcessAddress;
  private final FabucoProcessError errorInfo;
}
