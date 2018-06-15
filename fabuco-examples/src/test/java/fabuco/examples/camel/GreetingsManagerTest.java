package fabuco.examples.camel;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fabuco.process.ProcessExecutor;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class GreetingsManagerTest {

  @Mock
  private ProcessExecutor executor;

  @BeforeEach
  void setUpMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void goToCheckGreetingsResults() {
    final GreetingsManager process = new GreetingsManager();
    final Persons persons = new Persons(1, Arrays.asList("John", "Mary", "Kevin"));
    process.onStart(executor, persons);
    verify(executor, times(1)).stepTo("checkGreetingsResults", persons);
  }

}
