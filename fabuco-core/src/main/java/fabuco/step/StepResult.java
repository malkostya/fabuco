package fabuco.step;

import java.time.Duration;

public interface StepResult {

  StepResult withDelay(Duration delay);

  Duration getDelay();
}
