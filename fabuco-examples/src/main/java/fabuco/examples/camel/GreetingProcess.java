package fabuco.examples.camel;

import fabuco.performer.PerformerCall;
import fabuco.process.FabucoProcess;
import fabuco.process.ProcessExecutor;
import fabuco.step.StepResult;
import java.time.Duration;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class GreetingProcess implements FabucoProcess<GreetPerson, String> {

  private int stepCount = 0;

  @Override
  public StepResult onStart(ProcessExecutor<String> pe, GreetPerson param) {
    log.info("Step {}. GreetPerson {} started", ++stepCount, param.getPersonName());
    return pe.stepTo("greetWithDelay", param.getPersonName())
        .withDelay(Duration.ofSeconds(2));
  }

  public StepResult greetWithDelay(ProcessExecutor<String> pe, String personName) {
    log.info("Step {}. Say Hello {} with delay", ++stepCount, personName);
    pe.perform(new GetGreeting(personName));
    return pe.stepTo("showGreeting");
  }

  public StepResult showGreeting(ProcessExecutor<String> pe) {
    final PerformerCall<GetGreeting, String> call = pe.getPerformerCall(GetGreeting.class);
    if (call.isSucceeded()) {
      log.info("Step {}. {}", ++stepCount, call.getResult());
      return pe.complete(call.getResult());
    }
    log.info("Step {}. {}", ++stepCount, getFailMessage(call));
    return pe.fail("Failed to greet. Reason: " + getFailMessage(call));
  }

  private String getFailMessage(PerformerCall<GetGreeting, String> call) {
    if (call.isRecoverableError()) {
      return call.getRecoverableError().getMessage();
    } else if (call.isExpired()) {
      return "the greeting expired";
    } else {
      return call.getFaultCode();
    }
  }
}
