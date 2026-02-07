package org.atlas.libs.framework.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class NullUtil {

  public static <T> T nvl(T obj, T defaultVal) {
    return obj != null ? obj : defaultVal;
  }
}
