package fabuco.impl.util;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class InvokeMethodUtils {

  private static Field lookupsModifiersField;
  private static final int ALL_MODES = (MethodHandles.Lookup.PRIVATE
      | MethodHandles.Lookup.PROTECTED | MethodHandles.Lookup.PACKAGE
      | MethodHandles.Lookup.PUBLIC);

  public static <T> T createLambda(Method method, Class<T> interfaceClass, String signatureName) {
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup().in(method.getDeclaringClass());
      setAccessible(lookup);

      if (method.isAccessible()) {
        lookup = lookup.in(method.getDeclaringClass());
        setAccessible(lookup);
      }

      MethodHandle methodHandle = lookup.unreflect(method);
      MethodType instantiatedMethodType = methodHandle.type();
      MethodType signature = createLambdaMethodType(method, instantiatedMethodType);

      CallSite site = LambdaMetafactory.metafactory(
          lookup,
          signatureName,
          MethodType.methodType(interfaceClass),
          signature,
          methodHandle,
          instantiatedMethodType);

      MethodHandle factory = site.getTarget();
      return (T) factory.invoke();
    } catch (Throwable t) {
      throw new RuntimeException(t);
    }
  }

  private static void setAccessible(MethodHandles.Lookup lookup)
      throws NoSuchFieldException, IllegalAccessException {
    fillLookupsModifiersField();
    lookupsModifiersField.set(lookup, ALL_MODES);
  }

  private static MethodType createLambdaMethodType(Method method,
      MethodType instantiatedMethodType) {
    boolean isStatic = Modifier.isStatic(method.getModifiers());
    MethodType signature = isStatic ? instantiatedMethodType
        : instantiatedMethodType.changeParameterType(0, Object.class);

    Class<?>[] params = method.getParameterTypes();
    for (int i = 0; i < params.length; i++) {
      if (Object.class.isAssignableFrom(params[i])) {
        signature = signature.changeParameterType(isStatic ? i : i + 1, Object.class);
      }
    }
    if (Object.class.isAssignableFrom(signature.returnType())) {
      signature = signature.changeReturnType(Object.class);
    }

    return signature;
  }

  private static void fillLookupsModifiersField()
      throws NoSuchFieldException, IllegalAccessException {
    if (lookupsModifiersField == null || !lookupsModifiersField.isAccessible()) {
      Field modifiersField = Field.class.getDeclaredField("modifiers");
      modifiersField.setAccessible(true);

      Field allowedModesField = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
      allowedModesField.setAccessible(true);
      int modifiers = allowedModesField.getModifiers();
      modifiersField.setInt(allowedModesField, modifiers & ~Modifier.FINAL);

      lookupsModifiersField = allowedModesField;
    }
  }

}
