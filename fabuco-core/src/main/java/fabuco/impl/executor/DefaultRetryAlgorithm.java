package fabuco.impl.executor;

import fabuco.impl.core.FabucoConstants;
import fabuco.performer.RetryAlgorithm;
import java.time.Duration;

public class DefaultRetryAlgorithm implements RetryAlgorithm {

  public static final DefaultRetryAlgorithm INSTANCE = new DefaultRetryAlgorithm();
  private static final Duration[] durations = new Duration[] {
      FabucoConstants.ONE_SECOND, FabucoConstants.TWO_SECOND, FabucoConstants.FIVE_SECOND,
      FabucoConstants.TEN_SECOND, FabucoConstants.THIRTY_SECOND, FabucoConstants.ONE_MINUTE,
      FabucoConstants.TWO_MINUTE, FabucoConstants.FIVE_MINUTE, FabucoConstants.TEN_MINUTE
  };

  @Override
  public Duration nextRetry(int retryNumber) {
    if (retryNumber >= durations.length) {
      retryNumber = durations.length - 1;
    }
    return durations[retryNumber];
  }
}
