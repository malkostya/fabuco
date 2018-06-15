package fabuco.impl.executor;

@FunctionalInterface
public interface PerformerFunction<P, C> {

  void apply(P performer, C context);
}
