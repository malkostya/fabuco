package fabuco.impl.executor.data;

import fabuco.process.ProcessParameter;
import java.io.Serializable;
import lombok.Value;

@Value
public class SubProcessCompleteResponse<P extends ProcessParameter<R>, R> implements Serializable {

  private static final long serialVersionUID = -3999602945085576877L;
  private final ChildAddress<P> subProcessAddress;
  private final R result;
}
