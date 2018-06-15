package fabuco.impl.util;

@FunctionalInterface
public interface PerformerMethod<A, C, P> {

  Object apply(A adapter, C call, P parameter, Object s, Object i);
}
