package fabuco.impl.executor.data;

import fabuco.impl.storage.FabucoStorage;
import fabuco.performer.PerformerParameter;
import java.io.Serializable;
import lombok.Value;

@Value
public class PerformerCoordinatorInitCommand<P extends PerformerParameter<R>, R> implements
    Serializable {

  private static final long serialVersionUID = -6570773650348722015L;
  private final PerformerAttributes<P, R> attributes;
  private final FabucoStorage storage;
}
