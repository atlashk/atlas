package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.Saga;
import org.atlas.framework.saga.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.annotation.StartSaga;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.util.StringUtil;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Enhanced registry for sagas. Provides efficient caching and validation of saga configurations
 * with optimized reflection operations. Uses lazy initialization to avoid circular dependencies.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaRegistry {

  private final ApplicationContext applicationContext;

  // Thread-safe caches for saga metadata
  private final Map<String, SagaMetadata> sagaMetadataMap = new ConcurrentHashMap<>();

  // Cache for reflection results to avoid repeated scanning
  private final Map<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

  // Flag to track if sagas have been initialized
  private volatile boolean initialized = false;

  /**
   * Get saga metadata by saga name. Performs lazy initialization if needed.
   *
   * @param sagaName the name of the saga
   * @return Optional containing the saga metadata if found
   */
  public Optional<SagaMetadata> getSagaMetadata(String sagaName) {
    if (StringUtil.isBlank(sagaName)) {
      return Optional.empty();
    }
    ensureInitialized();
    return Optional.ofNullable(sagaMetadataMap.get(sagaName));
  }

  /**
   * Check if a saga exists for the given saga name. Performs lazy initialization if needed.
   *
   * @param sagaName the name of the saga
   * @return true if saga exists, false otherwise
   */
  public boolean hasSagaMetadata(String sagaName) {
    if (StringUtil.isBlank(sagaName)) {
      return false;
    }
    ensureInitialized();
    return sagaMetadataMap.containsKey(sagaName);
  }

  /**
   * Ensure sagas are initialized using double-checked locking pattern
   */
  private void ensureInitialized() {
    if (!initialized) {
      synchronized (this) {
        if (!initialized) {
          registerSagas();
          initialized = true;
          log.info("Registered {} saga metadata successfully", sagaMetadataMap.size());
        }
      }
    }
  }

  /**
   * Register all sagas from the application context
   */
  private void registerSagas() {
    Map<String, Object> sagaBeans = applicationContext.getBeansWithAnnotation(Saga.class);
    log.debug("Found {} potential saga beans", sagaBeans.size());
    if (sagaBeans.isEmpty()) {
      return;
    }

    sagaBeans.forEach((sagaBeanName, sagaBean) -> {
      try {
        registerSaga(sagaBean, sagaBeanName);
      } catch (Exception e) {
        throw new SagaConfigException(
            String.format("Failed to register saga for bean '%s': %s", sagaBeanName,
                e.getMessage()), e);
      }
    });
  }

  private void registerSaga(Object sagaBean, String beanName) {
    if (sagaBean == null) {
      throw new SagaConfigException("Saga bean cannot be null for bean: " + beanName);
    }

    // Get the target class to handle CGLIB proxies
    Class<?> sagaClass = AopUtils.isAopProxy(sagaBean)
        ? AopUtils.getTargetClass(sagaBean)
        : sagaBean.getClass();

    // Read Saga annotation
    Saga sagaAnnotation = sagaClass.getAnnotation(Saga.class);
    if (sagaAnnotation == null) {
      throw new SagaConfigException(
          String.format("Bean '%s' is missing @Saga annotation", beanName));
    }

    String sagaName = sagaAnnotation.sagaName();
    if (StringUtil.isBlank(sagaName)) {
      throw new SagaConfigException(
          String.format("Saga name cannot be empty for saga bean '%s'", beanName));
    }

    // Check for duplicate saga names
    if (sagaMetadataMap.containsKey(sagaName)) {
      throw new SagaConfigException(
          String.format("Duplicate saga name '%s' found in bean '%s'", sagaName, beanName));
    }

    // Discover and validate annotated methods
    Method startSagaMethod = findStartSagaMethod(sagaClass);
    List<Method> sagaCommandReplyHandlerMethods = findSagaCommandReplyHandlerMethods(sagaClass);

    // Validate method signatures
    validateStartSagaMethod(startSagaMethod, sagaName);
    for (Method sagaCommandReplyHandlerMethod : sagaCommandReplyHandlerMethods) {
      validateSagaCommandReplyHandlerMethod(sagaCommandReplyHandlerMethod, sagaName);
    }

    // Register saga metadata instance
    SagaMetadata sagaMetadata = SagaMetadata.builder()
        .sagaName(sagaName)
        .sagaBean(sagaBean)
        .startSagaMethod(startSagaMethod)
        .sagaCommandReplyHandlerMethods(sagaCommandReplyHandlerMethods)
        .build();

    sagaMetadataMap.put(sagaName, sagaMetadata);

    log.debug("Registered metadata for saga '{}' with {} reply handlers from bean '{}'",
        sagaName, sagaCommandReplyHandlerMethods.size(), beanName);
  }

  /**
   * Find the @StartSaga annotated method using cached reflection results
   */
  private Method findStartSagaMethod(Class<?> sagaClass) {
    List<Method> methods = getCachedMethods(sagaClass);

    List<Method> startSagaMethods = methods.stream()
        .filter(method -> method.isAnnotationPresent(StartSaga.class))
        .toList();

    if (startSagaMethods.isEmpty()) {
      throw new SagaConfigException(
          String.format("No @StartSaga method found in saga bean: %s", sagaClass.getName()));
    }

    if (startSagaMethods.size() > 1) {
      throw new SagaConfigException(
          String.format(
              "Multiple @StartSaga methods found in saga bean: %s. Only one is allowed.",
              sagaClass.getName()));
    }

    return startSagaMethods.get(0);
  }

  /**
   * Find all @SagaCommandReplyHandler annotated methods using cached reflection results
   */
  private List<Method> findSagaCommandReplyHandlerMethods(Class<?> sagaClass) {
    List<Method> methods = getCachedMethods(sagaClass);
    return methods.stream()
        .filter(method -> method.isAnnotationPresent(SagaCommandReplyHandler.class))
        .collect(Collectors.toList());
  }

  /**
   * Get cached methods for a class to avoid repeated reflection operations
   */
  private List<Method> getCachedMethods(Class<?> sagaClass) {
    return methodCache.computeIfAbsent(sagaClass, k -> Arrays.asList(k.getDeclaredMethods()));
  }

  /**
   * Validate the @StartSaga method signature
   */
  private void validateStartSagaMethod(Method method, String sagaName) {
    Parameter[] parameters = method.getParameters();

    boolean existSagaEntityParameter = false;
    for (Parameter parameter : parameters) {
      if (SagaEntity.class.isAssignableFrom(parameter.getType())) {
        existSagaEntityParameter = true;
      }
    }

    if (!existSagaEntityParameter) {
      throw new SagaConfigException(
          String.format("@StartSaga method in saga '%s' must have a parameter of type SagaEntity",
              sagaName));
    }
  }

  /**
   * Validate a single @SagaCommandReplyHandler method signature
   */
  private void validateSagaCommandReplyHandlerMethod(Method method, String sagaName) {
    Parameter[] parameters = method.getParameters();

    boolean existSagaEntityParameter = false;
    for (Parameter parameter : parameters) {
      if (SagaEntity.class.isAssignableFrom(parameter.getType())) {
        existSagaEntityParameter = true;
      }
    }

    if (!existSagaEntityParameter) {
      throw new SagaConfigException(
          String.format(
              "@SagaCommandReplyHandler method in saga '%s' must have a parameter of type SagaEntity",
              sagaName));
    }
  }
}
