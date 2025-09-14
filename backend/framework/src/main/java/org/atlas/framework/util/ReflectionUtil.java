package org.atlas.framework.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

@UtilityClass
@Slf4j
public class ReflectionUtil {

  /**
   * Invokes a method on an object without parameters
   *
   * @param target     the target object to invoke the method on
   * @param methodName the name of the method to invoke
   * @return the result of the method invocation, or null if an error occurs
   */
  public static Object invokeMethod(Object target, String methodName) {
    if (target == null || methodName == null) {
      return null;
    }

    try {
      Method method = target.getClass().getMethod(methodName);
      return method.invoke(target);
    } catch (Exception e) {
      log.error("Error while invoking method '{}' on object of type '{}'", methodName,
          target.getClass().getSimpleName(), e);
      return null;
    }
  }

  /**
   * Invokes a method on an object without parameters using a specific target class
   *
   * @param target      the target object to invoke the method on
   * @param targetClass the class to use for method lookup (useful for proxy objects)
   * @param methodName  the name of the method to invoke
   * @return the result of the method invocation, or null if an error occurs
   */
  public static Object invokeMethod(Object target, Class<?> targetClass, String methodName) {
    if (target == null || targetClass == null || methodName == null) {
      return null;
    }

    try {
      Method method = targetClass.getMethod(methodName);
      return method.invoke(target);
    } catch (Exception e) {
      log.error("Error while invoking method '{}' on object of type '{}' using target class '{}'", 
          methodName, target.getClass().getSimpleName(), targetClass.getSimpleName(), e);
      return null;
    }
  }

  /**
   * Invokes a method on an object using a Map of parameter types and values
   *
   * @param target     the target object to invoke the method on
   * @param methodName the name of the method to invoke
   * @param parameters a Map where keys are parameter types and values are the arguments
   * @return the result of the method invocation, or null if an error occurs
   */
  public static Object invokeMethod(Object target, String methodName,
      Map<Class<?>, Object> parameters) {
    if (target == null || methodName == null) {
      return null;
    }

    try {
      Class<?>[] parameterTypes = parameters.keySet().toArray(new Class<?>[0]);
      Object[] args = parameters.values().toArray();

      Method method = target.getClass().getMethod(methodName, parameterTypes);
      return method.invoke(target, args);
    } catch (Exception e) {
      log.error("Error while invoking method '{}' on object of type '{}'", methodName,
          target.getClass().getSimpleName(), e);
      return null;
    }
  }

  /**
   * Invokes a method on an object using a Map of parameter types and values with a specific target class
   *
   * @param target      the target object to invoke the method on
   * @param targetClass the class to use for method lookup (useful for proxy objects)
   * @param methodName  the name of the method to invoke
   * @param parameters  a Map where keys are parameter types and values are the arguments
   * @return the result of the method invocation, or null if an error occurs
   */
  public static Object invokeMethod(Object target, Class<?> targetClass, String methodName,
      Map<Class<?>, Object> parameters) {
    if (target == null || targetClass == null || methodName == null) {
      return null;
    }

    try {
      Class<?>[] parameterTypes = parameters.keySet().toArray(new Class<?>[0]);
      Object[] args = parameters.values().toArray();

      Method method = targetClass.getMethod(methodName, parameterTypes);
      return method.invoke(target, args);
    } catch (Exception e) {
      log.error("Error while invoking method '{}' on object of type '{}' using target class '{}'", 
          methodName, target.getClass().getSimpleName(), targetClass.getSimpleName(), e);
      return null;
    }
  }

  /**
   * Invokes a method on an object without parameters with type casting of the result
   *
   * @param target     the target object to invoke the method on
   * @param methodName the name of the method to invoke
   * @param returnType the expected return type
   * @return the result of the method invocation cast to the expected type, or null if casting fails
   */
  @SuppressWarnings("unchecked")
  public static <T> T invokeMethod(Object target, String methodName, Class<T> returnType) {
    Object result = invokeMethod(target, methodName);
    if (returnType.isInstance(result)) {
      return (T) result;
    }
    return null;
  }

  /**
   * Invokes a method on an object using a Map of parameter types and values with type casting of
   * the result
   *
   * @param target     the target object to invoke the method on
   * @param methodName the name of the method to invoke
   * @param parameters a Map where keys are parameter types and values are the arguments
   * @param returnType the expected return type
   * @return the result of the method invocation cast to the expected type, or null if casting fails
   */
  @SuppressWarnings("unchecked")
  public static <T> T invokeMethod(Object target, String methodName,
      Map<Class<?>, Object> parameters, Class<T> returnType) {
    Object result = invokeMethod(target, methodName, parameters);
    if (returnType.isInstance(result)) {
      return (T) result;
    }
    return null;
  }

  /**
   * Gets the value of an annotation attribute by attribute name with type casting
   *
   * @param annotation    the annotation instance
   * @param attributeName the name of the attribute to retrieve
   * @param attributeType the type of the attribute to retrieve
   * @return the attribute value cast to the attribute type, or null otherwise
   */
  @SuppressWarnings("unchecked")
  public static <T> T getAnnotationAttributeValue(Annotation annotation, String attributeName,
      Class<T> attributeType) {
    if (annotation == null || attributeName == null) {
      return null;
    }

    try {
      Method method = annotation.annotationType().getMethod(attributeName);
      Object value = method.invoke(annotation);
      if (attributeType.isInstance(value)) {
        return (T) value;
      }
    } catch (Exception e) {
      log.error("Error while getting annotation attribute value", e);
    }

    return null;
  }
}
