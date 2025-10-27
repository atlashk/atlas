package org.atlas.framework.util;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;

@UtilityClass
public class ExceptionUtil {

  public static String sanitizeErrorMessage(Throwable t) {
    return sanitizeErrorMessage(t.getMessage());
  }

  public static String sanitizeErrorMessage(String errorMessage) {
    if (StringUtil.isBlank(errorMessage)) {
      return "Unknown error";
    }

    // Limit length to prevent database issues
    if (errorMessage.length() > 1000) {
      return errorMessage.substring(0, 997) + "...";
    }

    return errorMessage;
  }

  public static Throwable getRootCause(Exception e) {
    return ExceptionUtils.getRootCause(e);
  }
}
