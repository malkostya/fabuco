package fabuco.impl.executor.data;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Value
@EqualsAndHashCode
public class ChildAddress<P> implements Serializable {

  private static final long serialVersionUID = 1734696127793573587L;
  private Class<P> parameterType;
  private String index;
}
