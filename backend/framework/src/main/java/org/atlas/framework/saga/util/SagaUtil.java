package org.atlas.framework.saga.util;

import java.lang.reflect.Method;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaStep;
import org.atlas.framework.util.StringUtil;

/**
 * Utility class for common saga operations and helper methods
 */
@UtilityClass
@Slf4j
public class SagaUtil {

  /**
   * Create error message with context
   */
  public static String createErrorMessage(String operation, Long sagaId, String stepName,
      Throwable error) {
    StringBuilder sb = new StringBuilder();
    sb.append("Error in ").append(operation);
    if (sagaId != null) {
      sb.append(" for saga: ").append(sagaId);
    }
    if (StringUtil.isNotBlank(stepName)) {
      sb.append(", step: ").append(stepName);
    }
    if (error != null) {
      sb.append(" - ").append(error.getMessage());
    }
    return sb.toString();
  }

  /**
   * Get step max retries from annotation
   */
  public static int getStepMaxRetries(Method stepMethod) {
    if (stepMethod.isAnnotationPresent(SagaStep.class)) {
      SagaStep annotation = stepMethod.getAnnotation(SagaStep.class);
      return annotation.maxRetries() >= 0 ? annotation.maxRetries() : 3; // Default 3 retries
    }
    return 3; // Default retries
  }
}