package fabuco.examples.camel;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fabuco.impl.executor.data.FabucoStepResult;
import fabuco.process.FakeProcessExecutor;
import fabuco.process.ProcessExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


public class GreetingProcessTest {

  private static final String PERSON_NAME = "John";

  @Mock
  private ProcessExecutor executor;// = new FakeProcessExecutor();

  @Mock
  private FabucoStepResult stepResult;

  @BeforeEach
  void setUpMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void goToGreetWithDelay() {
    when(executor.stepTo("greetWithDelay", PERSON_NAME)).thenReturn(stepResult);
    final GreetingProcess process = new GreetingProcess();
    final GreetPerson greetPerson = new GreetPerson(PERSON_NAME);
    process.onStart(executor, greetPerson);
    verify(executor, times(1)).stepTo("greetWithDelay", PERSON_NAME);
  }

  @Test
  @Disabled
  public void testForComplete() {
    GreetingProcess process = new GreetingProcess();
    FakeProcessExecutor fakeExecutor = new FakeProcessExecutor(process);
    GreetPerson param = new GreetPerson(PERSON_NAME);
    process.onStart(fakeExecutor, param);
    // TODO: need to complete after FakeProcessExecutor is ready
  }
}
