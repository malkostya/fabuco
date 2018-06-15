package fabuco.performer;

import java.time.Duration;

public interface RetryAlgorithm {

  Duration nextRetry(int retryNumber);
}
