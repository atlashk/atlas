package org.atlas.framework.saga.util;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaOrchestrator;
import org.atlas.framework.saga.annotation.SagaStep;
import org.atlas.framework.util.StringUtil;

/**
 * Utility class for common saga operations and helper methods
 */
@UtilityClass
@Slf4j
public class SagaUtil {

  private static final DateTimeFormatter SAGA_ID_FORMATTER = DateTimeFormatter.ofPattern(
      "yyyyMMddHHmmss");
  private static final Map<Class<?>, List<Method>> STEP_METHOD_CACHE = new ConcurrentHashMap<>();

  /**
   * Generate a unique saga ID
   */
  public static Long generateSagaId() {
    String timestamp = LocalDateTime.now().format(SAGA_ID_FORMATTER);
    String uuid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
    return Long.parseLong(timestamp + uuid.substring(0, 4));
  }

  /**
   * Generate saga name from orchestrator class
   */
  public static String generateSagaName(Class<?> orchestratorClass) {
    if (orchestratorClass.isAnnotationPresent(SagaOrchestrator.class)) {
      SagaOrchestrator annotation = orchestratorClass.getAnnotation(SagaOrchestrator.class);
      if (StringUtil.isNotBlank(annotation.name())) {
        return annotation.name();
      }
    }

    // Generate from class name
    String className = orchestratorClass.getSimpleName();
    if (className.endsWith("Orchestrator")) {
      className = className.substring(0, className.length() - 12); // Remove "Orchestrator"
    }

    return camelCaseToSnakeCase(className) + "_saga";
  }

  /**
   * Get all step methods from orchestrator class
   */
  public static List<Method> getStepMethods(Class<?> orchestratorClass) {
    return STEP_METHOD_CACHE.computeIfAbsent(orchestratorClass, clazz -> {
      return Arrays.stream(clazz.getDeclaredMethods())
          .filter(method -> method.isAnnotationPresent(SagaStep.class))
          .sorted((m1, m2) -> {
            SagaStep step1 = m1.getAnnotation(SagaStep.class);
            SagaStep step2 = m2.getAnnotation(SagaStep.class);
            return Integer.compare(step1.order(), step2.order());
          })
          .collect(Collectors.toList());
    });
  }

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

  /**
   * Check if step is required
   */
  public static boolean isStepRequired(Method stepMethod) {
    if (stepMethod.isAnnotationPresent(SagaStep.class)) {
      SagaStep annotation = stepMethod.getAnnotation(SagaStep.class);
      return annotation.required();
    }
    return true; // Default to required
  }

  // Private helper methods

  private static String camelCaseToSnakeCase(String camelCase) {
    return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
  }
}