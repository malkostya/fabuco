package fabuco.impl.executor.step.function;

@FunctionalInterface
public interface StepArg3<S, P, E> {

  S apply(P process, E executor, Object arg1, Object arg2, Object arg3);
}
