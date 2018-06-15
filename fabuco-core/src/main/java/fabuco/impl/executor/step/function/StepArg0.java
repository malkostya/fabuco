package fabuco.impl.executor.step.function;

@FunctionalInterface
public interface StepArg0<S, P, E> {

  S apply(P process, E executor);
}
