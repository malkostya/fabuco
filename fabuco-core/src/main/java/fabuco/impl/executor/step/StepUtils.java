package fabuco.impl.executor.step;

import fabuco.process.ProcessExecutor;
import fabuco.impl.executor.step.wrapper.StepArg0Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg10Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg1Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg2Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg3Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg4Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg5Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg6Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg7Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg8Wrapper;
import fabuco.impl.executor.step.wrapper.StepArg9Wrapper;
import fabuco.impl.executor.step.wrapper.StepWrapper;
import fabuco.step.StepResult;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StepUtils {

  private static final int ALL_MODES = Modifier.STATIC
      | Modifier.PUBLIC | Modifier.FINAL;

  private static final StepWrapper[] STEP_WRAPPERS_CLASSES = new StepWrapper[]{
      StepArg0Wrapper.getInstance(), StepArg1Wrapper.getInstance(), StepArg2Wrapper.getInstance(),
      StepArg3Wrapper.getInstance(), StepArg4Wrapper.getInstance(), StepArg5Wrapper.getInstance(),
      StepArg6Wrapper.getInstance(), StepArg7Wrapper.getInstance(), StepArg8Wrapper.getInstance(),
      StepArg9Wrapper.getInstance(), StepArg10Wrapper.getInstance()
  };

  public static Map<String, List<StepWrapper>> getProcessStepsInfo(Class processType) {
    Map<String, List<StepWrapper>> stepsInfo = new HashMap<>();

    getStepMethods(processType).stream().forEach(
        method -> {
          String methodName = method.getName();

          if (method.getParameterCount() == 0
              || !method.getParameterTypes()[0].equals(ProcessExecutor.class)
              || !method.getReturnType().equals(StepResult.class)) {
            return;
          }

          if (method.getParameterCount() > STEP_WRAPPERS_CLASSES.length) {
            new RuntimeException(
                "Number of arguments of the step function " + methodName + " succeeded "
                    + STEP_WRAPPERS_CLASSES.length);
          }

          List<StepWrapper> stepWrappers = stepsInfo.get(methodName);

          if (stepWrappers == null) {
            stepWrappers = new ArrayList<>();
            stepsInfo.put(methodName, stepWrappers);
          }

          StepWrapper wrapper = STEP_WRAPPERS_CLASSES[method.getParameterCount() - 1]
              .getWrapperForMethod(method);
          stepWrappers.add(wrapper);
        }
    );

    return stepsInfo;
  }

  private static List<Method> getStepMethods(Class processType) {
    List<Method> methods = new ArrayList<>();
    for (Method method : processType.getMethods()) {
      if ((method.getModifiers() | ALL_MODES) == ALL_MODES) {
        methods.add(method);
      }
    }

    return methods;
  }


}
