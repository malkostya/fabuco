package fabuco.impl.executor.data;

import fabuco.impl.executor.FabucoPerformerCall;
import fabuco.performer.PerformerParameter;
import java.io.Serializable;
import lombok.Value;

@Value
public class PerformerCompleteResponse<P extends PerformerParameter<R>, R> implements Serializable {

  private static final long serialVersionUID = -8663327696733681594L;
  private final ChildAddress<P> performerAddress;
  private final FabucoPerformerCall<P, R> call;
}
