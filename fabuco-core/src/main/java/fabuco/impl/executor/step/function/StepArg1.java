package fabuco.impl.executor.step.function;

@FunctionalInterface
public interface StepArg1<S, P, E> {

  S apply(P process, E executor, Object arg1);
}
