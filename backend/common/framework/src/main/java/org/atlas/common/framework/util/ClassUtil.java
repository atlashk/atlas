package org.atlas.common.framework.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ClassUtil {

  public static boolean isExist(String classPath) {
    try {
      Class.forName(classPath);
      return true;
    } catch (ClassNotFoundException e) {
      return false;
    }
  }

  public static Class<?> getClass(String classPath) {
    try {
      return Class.forName(classPath);
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

  public static boolean isSubclass(Class<?> superClass, Class<?> subClass) {
    return superClass.isAssignableFrom(subClass) && superClass != subClass;
  }
}
