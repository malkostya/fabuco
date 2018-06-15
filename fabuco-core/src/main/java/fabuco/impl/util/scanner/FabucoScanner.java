package fabuco.impl.util.scanner;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import fabuco.process.FabucoProcess;
import fabuco.impl.annotation.Performer;
import fabuco.impl.annotation.PerformerDestroy;
import fabuco.impl.annotation.PerformerInit;
import fabuco.impl.annotation.PerformerMethod;
import fabuco.impl.executor.PerformerFunction;
import fabuco.impl.util.InvokeMethodUtils;
import fabuco.performer.PerformerCallContext;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;

@Slf4j
public class FabucoScanner {

  private Reflections reflections;

  public FabucoScanner(String prefix) {
    reflections = new Reflections(prefix, new MethodAnnotationsScanner(),
        new TypeAnnotationsScanner(), new SubTypesScanner());
  }

  public List<ProcessInfo> getProcessInfos() {
    final Map<Class<? extends FabucoProcess>, ProcessInfo> infos = reflections
        .getSubTypesOf(FabucoProcess.class)
        .stream()
        .collect(toMap(
            Function.identity(),
            ProcessInfo::new
        ));
    infos.values().stream().forEach(info -> {
      ProcessInfo parentInfo = infos.get(info.getProcessType().getSuperclass());
      if (parentInfo != null) {
        info.setParentInfo(parentInfo);
        parentInfo.setHasChildren();
      }
      setProcessInfoTypes(info);
    });
    infos.values()
        .stream()
        .filter(info -> info.getParameterType() == null)
        .forEach(info -> setProcessInfoTypesFromParent(info));
    return infos.values()
        .stream()
        .filter(info -> info.isChildFree() && info.getParameterType() != null)
        .collect(toList());
  }

  public PerformerInfos getPerformerInfos() {
    Set<Class<?>> performerTypes = reflections.getTypesAnnotatedWith(Performer.class);

    if (!performerTypes.isEmpty()) {
      final List<PerformerInfo> performerInfoList = performerTypes.stream()
          .map(performerType -> {
            try {
              final Object performer = performerType.newInstance();
              final Method performerInitMethod = getPerformerInitMethod(performer);
              final Method performerDestroyMethod = getPerformerDestroyMethod(performer);
              return new PerformerInfo(performer, performerInitMethod, performerDestroyMethod);
            } catch (InstantiationException | IllegalAccessException e) {
              throw new RuntimeException(e);
            }
          }).collect(toList());

      final List<PerformerMethodInfo> performerMethodInfos = performerInfoList.stream()
          .flatMap(performerInfo -> {
            return getDeclaredMethodsAsStream(performerInfo.getPerformer().getClass()).filter(
                method -> {
                  if (method.getDeclaredAnnotation(PerformerMethod.class) == null) {
                    return false;
                  }
                  if (method.getParameterCount() != 1) {
                    return false;
                  }
                  if (!method.getReturnType().equals(Void.TYPE)) {
                    return false;
                  }
                  final Parameter param = method.getParameters()[0];
                  return param.getType().equals(PerformerCallContext.class)
                      && param.getParameterizedType() instanceof ParameterizedType;
                }).map(method -> {
              final Parameter param = method.getParameters()[0];
              final ParameterizedType type = (ParameterizedType) param.getParameterizedType();
              final Type[] typeArguments = type.getActualTypeArguments();
              final Class parameterType = (Class) typeArguments[0];
              final Class resultType = (Class) typeArguments[1];
              final PerformerFunction<Object, PerformerCallContext> performerMethod =
                  InvokeMethodUtils.createLambda(method, PerformerFunction.class, "apply");
              return new PerformerMethodInfo(parameterType, resultType,
                  performerInfo.getPerformer(), performerMethod);
            });
          }).collect(toList());

      return new PerformerInfos(performerInfoList, performerMethodInfos);
    }

    return new PerformerInfos(Collections.emptyList(), Collections.emptyList());
  }

  private static Method getPerformerInitMethod(Object performer) {
    return getDeclaredMethodsAsStream(performer.getClass()).filter(method ->
        method.getDeclaredAnnotation(PerformerInit.class) != null
            && method.getParameterCount() == 0 && method.getReturnType().equals(Void.TYPE)
    ).findAny().orElse(null);
  }

  private static Method getPerformerDestroyMethod(Object performer) {
    return getDeclaredMethodsAsStream(performer.getClass()).filter(method ->
        method.getDeclaredAnnotation(PerformerDestroy.class) != null
            && method.getParameterCount() == 0 && method.getReturnType().equals(Void.TYPE)
    ).findAny().orElse(null);
  }

  private static Stream<Method> getDeclaredMethodsAsStream(Class clazz) {
    return Arrays.stream(clazz.getDeclaredMethods());
  }

  private void setProcessInfoTypes(ProcessInfo info) {
    for (AnnotatedType annotatedType : info.getProcessType().getAnnotatedInterfaces()) {
      final ParameterizedType type = (ParameterizedType) annotatedType.getType();
      if (type.getRawType().equals(FabucoProcess.class)) {
        final Type[] typeArguments = type.getActualTypeArguments();
        info.setParameterType((Class) typeArguments[0]);
        info.setResultType((Class) typeArguments[1]);
        return;
      }
    }
  }

  private void setProcessInfoTypesFromParent(ProcessInfo info) {
    ProcessInfo parentInfo = info.getParentInfo();
    while (parentInfo != null) {
      if (parentInfo.getParameterType() != null) {
        info.setParameterType(parentInfo.getParameterType());
        info.setResultType(parentInfo.getResultType());
        return;
      }
      parentInfo = parentInfo.getParentInfo();
    }
  }
}
