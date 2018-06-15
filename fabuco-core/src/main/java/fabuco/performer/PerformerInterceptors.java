package fabuco.performer;

public class PerformerInterceptors {

  public static final PerformerInterceptor RETRY_IF_ERROR =
      call -> call.isRecoverableError()
          ? InterceptorResult.RETRY_BY_DEFAULT_ALGORITHM
          : InterceptorResult.RETURN_RESULT;

  public static final PerformerInterceptor ALWAYS_RETURN_RESULT =
      call -> InterceptorResult.RETURN_RESULT;

  public static final PerformerInterceptor retryByAlgorithmIfError(RetryAlgorithm retryAlgorithm) {
    return call -> call.isRecoverableError()
        ? InterceptorResult.retryByAlgorithm(retryAlgorithm)
        : InterceptorResult.RETURN_RESULT;
  }
}
