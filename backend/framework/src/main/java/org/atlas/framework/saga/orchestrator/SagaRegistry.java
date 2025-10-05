package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaCommandReplyHandler;
import org.atlas.framework.saga.annotation.Saga;
import org.atlas.framework.saga.annotation.StartSaga;
import org.atlas.framework.saga.context.SagaContext;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Enhanced registry for saga orchestrators and their steps. Provides efficient caching and
 * validation of saga configurations with optimized reflection operations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaRegistry implements InitializingBean {

  private final ApplicationContext applicationContext;

  // Thread-safe caches for saga orchestrator metadata
  private final Map<String, SagaMetadata> orchestrators = new ConcurrentHashMap<>();

  // Cache for reflection results to avoid repeated scanning
  private final Map<Class<?>, List<Method>> methodCache = new ConcurrentHashMap<>();

  /**
   * Initialize the registry by discovering and registering all saga orchestrators
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    registerOrchestrators();
    log.info("Registered {} saga orchestrators successfully", orchestrators.size());
  }

  /**
   * Get orchestrator metadata by saga name
   *
   * @param sagaName the name of the saga
   * @return Optional containing the orchestrator metadata if found
   */
  public Optional<SagaMetadata> getOrchestrator(String sagaName) {
    if (StringUtil.isBlank(sagaName)) {
      return Optional.empty();
    }
    return Optional.ofNullable(orchestrators.get(sagaName));
  }

  /**
   * Check if an orchestrator exists for the given saga name
   *
   * @param sagaName the name of the saga
   * @return true if orchestrator exists, false otherwise
   */
  public boolean hasOrchestrator(String sagaName) {
    return StringUtil.isNotBlank(sagaName) && orchestrators.containsKey(sagaName);
  }

  /**
   * Get all registered saga names for monitoring and debugging
   *
   * @return Set of all registered saga names
   */
  public Set<String> getAllSagaNames() {
    return Set.copyOf(orchestrators.keySet());
  }

  /**
   * Get the total number of registered orchestrators
   *
   * @return number of registered orchestrators
   */
  public int getOrchestratorCount() {
    return orchestrators.size();
  }

  /**
   * Register all saga orchestrators from the application context
   */
  private void registerOrchestrators() {
    Map<String, Object> orchestratorBeans = applicationContext.getBeansWithAnnotation(
        Saga.class);
    log.debug("Found {} potential saga orchestrator beans", orchestratorBeans.size());

    if (orchestratorBeans.isEmpty()) {
      log.warn("No saga orchestrator beans found in application context");
      return;
    }

    orchestratorBeans.forEach((beanName, bean) -> {
      try {
        registerOrchestrator(bean, beanName);
      } catch (Exception e) {
        throw new SagaConfigException(
            String.format("Failed to register saga orchestrator for bean '%s': %s",
                beanName, e.getMessage()), e);
      }
    });
  }

  /**
   * Register a single orchestrator and its steps
   */
  private void registerOrchestrator(Object orchestratorBean, String beanName) {
    if (orchestratorBean == null) {
      throw new SagaConfigException("Orchestrator bean cannot be null for bean: " + beanName);
    }

    Class<?> orchestratorClass = orchestratorBean.getClass();
    Saga sagaAnnotation = orchestratorClass.getAnnotation(
        Saga.class);

    if (sagaAnnotation == null) {
      throw new SagaConfigException(
          String.format("Bean '%s' is missing @SagaOrchestrator annotation", beanName));
    }

    String sagaName = sagaAnnotation.sagaName();
    if (StringUtil.isBlank(sagaName)) {
      throw new SagaConfigException(
          String.format("Saga name cannot be empty for orchestrator bean '%s'", beanName));
    }

    // Check for duplicate orchestrator names
    if (orchestrators.containsKey(sagaName)) {
      throw new SagaConfigException(
          String.format("Duplicate saga orchestrator name '%s' found in bean '%s'", sagaName,
              beanName));
    }

    // Discover and validate annotated methods
    Method startSagaMethod = findStartSagaMethod(orchestratorClass);
    List<Method> sagaCommandReplyHandlerMethods = findSagaCommandReplyHandlerMethods(
        orchestratorClass);

    // Validate method signatures
    validateStartSagaMethod(startSagaMethod, sagaName);
    for (Method sagaCommandReplyHandlerMethod : sagaCommandReplyHandlerMethods) {
      validateSagaCommandReplyHandlerMethod(sagaCommandReplyHandlerMethod, sagaName);
    }

    // Register orchestrator instance
    SagaMetadata sagaMetadata = SagaMetadata.builder()
        .sagaName(sagaName)
        .orchestratorInstance(orchestratorBean)
        .startSagaMethod(startSagaMethod)
        .sagaCommandReplyHandlerMethods(sagaCommandReplyHandlerMethods)
        .build();

    orchestrators.put(sagaName, sagaMetadata);

    log.debug("Registered orchestrator for saga '{}' with {} reply handlers from bean '{}'",
        sagaName, sagaCommandReplyHandlerMethods.size(), beanName);
  }

  /**
   * Find the @StartSaga annotated method using cached reflection results
   */
  private Method findStartSagaMethod(Class<?> orchestratorClass) {
    List<Method> methods = getCachedMethods(orchestratorClass);

    List<Method> startSagaMethods = methods.stream()
        .filter(method -> method.isAnnotationPresent(StartSaga.class))
        .toList();

    if (startSagaMethods.isEmpty()) {
      throw new SagaConfigException(
          String.format("No @StartSaga method found in orchestrator: %s",
              orchestratorClass.getName()));
    }

    if (startSagaMethods.size() > 1) {
      throw new SagaConfigException(
          String.format(
              "Multiple @StartSaga methods found in orchestrator: %s. Only one is allowed.",
              orchestratorClass.getName()));
    }

    return startSagaMethods.get(0);
  }

  /**
   * Find all @SagaCommandReplyHandler annotated methods using cached reflection results
   */
  private List<Method> findSagaCommandReplyHandlerMethods(Class<?> orchestratorClass) {
    List<Method> methods = getCachedMethods(orchestratorClass);

    return methods.stream()
        .filter(method -> method.isAnnotationPresent(SagaCommandReplyHandler.class))
        .collect(Collectors.toList());
  }

  /**
   * Get cached methods for a class to avoid repeated reflection operations
   */
  private List<Method> getCachedMethods(Class<?> clazz) {
    return methodCache.computeIfAbsent(clazz, k -> Arrays.asList(k.getDeclaredMethods()));
  }

  /**
   * Validate the @StartSaga method signature
   */
  private void validateStartSagaMethod(Method method, String sagaName) {
    Parameter[] parameters = method.getParameters();

    if (parameters.length != 1) {
      throw new SagaConfigException(
          String.format(
              "@StartSaga method in saga '%s' must have exactly one parameter of type SagaContext, but found %d parameters",
              sagaName, parameters.length));
    }

    if (!SagaContext.class.isAssignableFrom(parameters[0].getType())) {
      throw new SagaConfigException(
          String.format(
              "@StartSaga method in saga '%s' must have a parameter of type SagaContext, but found: %s",
              sagaName, parameters[0].getType().getName()));
    }

    if (method.getReturnType() != void.class) {
      throw new SagaConfigException(
          String.format("@StartSaga method in saga '%s' must return void, but returns: %s",
              sagaName, method.getReturnType().getName()));
    }
  }

  /**
   * Validate a single @SagaCommandReplyHandler method signature
   */
  private void validateSagaCommandReplyHandlerMethod(Method method, String sagaName) {
    Parameter[] parameters = method.getParameters();

    if (parameters.length != 1) {
      throw new SagaConfigException(
          String.format(
              "@SagaCommandReplyHandler method '%s' in saga '%s' must have exactly one parameter of type SagaContext, but found %d parameters",
              method.getName(), sagaName, parameters.length));
    }

    if (!SagaContext.class.isAssignableFrom(parameters[0].getType())) {
      throw new SagaConfigException(
          String.format(
              "@SagaCommandReplyHandler method '%s' in saga '%s' must have a parameter of type SagaContext, but found: %s",
              method.getName(), sagaName, parameters[0].getType().getName()));
    }

    if (method.getReturnType() != void.class) {
      throw new SagaConfigException(
          String.format(
              "@SagaCommandReplyHandler method '%s' in saga '%s' must return void, but returns: %s",
              method.getName(), sagaName, method.getReturnType().getName()));
    }

    // Validate that the annotation has a command specified
    SagaCommandReplyHandler annotation = method.getAnnotation(SagaCommandReplyHandler.class);
    if (annotation.command() == null) {
      throw new SagaConfigException(
          String.format("@SagaCommandReplyHandler method '%s' in saga '%s' must specify a command",
              method.getName(), sagaName));
    }
  }
}
