package fabuco.impl.util.scanner;

import fabuco.impl.executor.PerformerFunction;
import fabuco.performer.PerformerCallContext;
import fabuco.performer.PerformerParameter;
import lombok.Value;

@Value
public class PerformerMethodInfo<P extends PerformerParameter<R>, R> {

  private final Class<P> parameterType;
  private final Class<R> resultType;
  private final Object performer;
  private final PerformerFunction<Object, PerformerCallContext<P, R>> function;
}
