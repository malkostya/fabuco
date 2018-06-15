package fabuco.performer;

import lombok.Getter;

@Getter
public final class InterceptorResult {

  public static final InterceptorResult RETURN_RESULT = new InterceptorResult(
      ResultType.RETURN_RESULT, null);
  public static final InterceptorResult RETRY_BY_DEFAULT_ALGORITHM = new InterceptorResult(
      ResultType.RETRY_BY_DEFAULT_ALGORITHM, null);

  private final ResultType type;
  private final RetryAlgorithm retryAlgorithm;

  private InterceptorResult(ResultType type, RetryAlgorithm retryAlgorithm) {
    this.type = type;
    this.retryAlgorithm = retryAlgorithm;
  }

  public static InterceptorResult retryByAlgorithm(RetryAlgorithm retryAlgorithm) {
    return new InterceptorResult(ResultType.RETRY_BY_GIVEN_ALGORITHM, retryAlgorithm);
  }

  public enum ResultType {
    RETURN_RESULT,
    RETRY_BY_DEFAULT_ALGORITHM,
    RETRY_BY_GIVEN_ALGORITHM;
  }
}
