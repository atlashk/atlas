package org.atlas.framework.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;

@UtilityClass
public class ExceptionUtil {

  public static Throwable getRootCause(Exception e) {
    return ExceptionUtils.getRootCause(e);
  }
}
