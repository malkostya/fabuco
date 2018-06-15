package fabuco.impl.executor.step.function;

@FunctionalInterface
public interface StepArg6<S, P, E> {

  S apply(P process, E executor, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5,
      Object arg6);
}
