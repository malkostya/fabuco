package fabuco.examples.camel;

import fabuco.process.FabucoProcess;
import fabuco.process.ProcessCall;
import fabuco.process.ProcessExecutor;
import fabuco.step.StepResult;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
public class GreetingsManager implements FabucoProcess<Persons, String> {

  @Override
  public StepResult onStart(ProcessExecutor<String> pe, Persons persons) {
    persons.getNames().stream().forEach(name -> {
      pe.process(new GreetPerson(name), name);
    });
    return pe.stepTo("checkGreetingsResults", persons);
  }

  public StepResult checkGreetingsResults(ProcessExecutor<String> pe, Persons persons) {
    boolean success = !persons.getNames().stream().anyMatch(name -> {
      ProcessCall<GreetPerson, String> result = pe.getProcessCall(GreetPerson.class, name);
      return !result.succeeded();
    });

    if (success) {
      return pe.complete("All greetings succeeded");
    }
    return pe.fail("Something goes wrong");
  }
}
