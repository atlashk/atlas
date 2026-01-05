package org.atlas.common.framework.error;

import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.atlas.common.framework.util.StringUtil;

@UtilityClass
public class ErrorUtil {

  public static String sanitizeErrorMessage(Throwable t) {
    return sanitizeErrorMessage(t.getMessage());
  }

  public static String sanitizeErrorMessage(String errorMessage) {
    if (StringUtil.isBlank(errorMessage)) {
      return "Unknown error";
    }

    // Limit length to prevent database issues
    if (errorMessage.length() > 500) {
      return errorMessage.substring(0, 447) + "...";
    }

    return errorMessage;
  }

  public static String buildErrorMessage(String errorCode, String errorMessage) {
    if (StringUtil.isBlank(errorCode)) {
      return sanitizeErrorMessage(errorMessage);
    } else if (StringUtil.isBlank(errorMessage)) {
      return errorCode;
    } else {
      return String.format("%s:%s", errorCode, sanitizeErrorMessage(errorMessage));
    }
  }

  public static Throwable getRootCause(Exception e) {
    return ExceptionUtils.getRootCause(e);
  }
}
