package fabuco.performer;

@FunctionalInterface
public interface PerformerInterceptor<P extends PerformerParameter<R>, R> {

  InterceptorResult apply(PerformerCall<P, R> call);
}
