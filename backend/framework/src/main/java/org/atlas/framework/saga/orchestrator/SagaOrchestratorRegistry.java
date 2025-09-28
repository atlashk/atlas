package org.atlas.framework.saga.orchestrator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaOrchestrator;
import org.atlas.framework.saga.annotation.SagaStep;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Enhanced registry for saga orchestrators and their steps. Provides efficient caching and
 * validation of saga configurations.
 */
@Component
@Slf4j
public class SagaOrchestratorRegistry implements InitializingBean {

  private final ApplicationContext applicationContext;

  // Thread-safe caches for orchestrator metadata
  private final Map<String, Object> orchestratorInstances = new ConcurrentHashMap<>();
  private final Map<String, List<SagaStepInfo>> orchestratorSteps = new ConcurrentHashMap<>();
  private final Map<String, SagaOrchestratorInfo> orchestratorMetadata = new ConcurrentHashMap<>();

  // Performance optimization caches
  private final Map<String, Method> stepMethodCache = new ConcurrentHashMap<>();
  private final Map<String, Method> compensationMethodCache = new ConcurrentHashMap<>();
  private final Set<String> registeredOrchestratorNames = ConcurrentHashMap.newKeySet();

  // Registry statistics
  private volatile int totalOrchestrators = 0;
  private volatile int totalSteps = 0;
  private volatile long lastRegistrationTime = 0;

  @Autowired
  public SagaOrchestratorRegistry(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  /**
   * Initialize the registry by discovering and registering all saga orchestrators
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    long startTime = System.currentTimeMillis();
    log.info("Initializing Saga Orchestrator Registry...");

    try {
      discoverAndRegisterOrchestrators();
      validateRegistrations();

      long duration = System.currentTimeMillis() - startTime;
      lastRegistrationTime = System.currentTimeMillis();

      log.info("Saga Orchestrator Registry initialized successfully in {}ms. " +
              "Registered {} orchestrators with {} total steps",
          duration, totalOrchestrators, totalSteps);

    } catch (Exception e) {
      log.error("Failed to initialize Saga Orchestrator Registry", e);
      throw new IllegalStateException("Saga Orchestrator Registry initialization failed", e);
    }
  }

  /**
   * Discover and register all saga orchestrators from the application context
   */
  private void discoverAndRegisterOrchestrators() {
    Map<String, Object> orchestratorBeans = applicationContext.getBeansWithAnnotation(
        SagaOrchestrator.class);

    log.debug("Found {} potential saga orchestrator beans", orchestratorBeans.size());

    for (Map.Entry<String, Object> entry : orchestratorBeans.entrySet()) {
      String beanName = entry.getKey();
      Object orchestratorBean = entry.getValue();

      try {
        registerOrchestrator(beanName, orchestratorBean);
      } catch (Exception e) {
        log.error("Failed to register saga orchestrator: {}", beanName, e);
        throw new IllegalStateException("Failed to register orchestrator: " + beanName, e);
      }
    }

    totalOrchestrators = orchestratorInstances.size();
  }

  /**
   * Register a single orchestrator and its steps
   */
  private void registerOrchestrator(String beanName, Object orchestratorBean) {
    Class<?> orchestratorClass = orchestratorBean.getClass();
    SagaOrchestrator annotation = orchestratorClass.getAnnotation(SagaOrchestrator.class);

    if (annotation == null) {
      log.warn("Bean {} does not have @SagaOrchestrator annotation", beanName);
      return;
    }

    String orchestratorName = annotation.name().isEmpty() ? beanName : annotation.name();

    // Check for duplicate orchestrator names
    if (registeredOrchestratorNames.contains(orchestratorName)) {
      throw new IllegalStateException("Duplicate saga orchestrator name: " + orchestratorName);
    }

    // Register orchestrator instance
    orchestratorInstances.put(orchestratorName, orchestratorBean);
    registeredOrchestratorNames.add(orchestratorName);

    // Discover and register steps
    List<SagaStepInfo> steps = discoverSteps(orchestratorClass, orchestratorBean);
    orchestratorSteps.put(orchestratorName, steps);

    // Create orchestrator metadata
    SagaOrchestratorInfo metadata = SagaOrchestratorInfo.builder()
        .name(orchestratorName)
        .beanName(beanName)
        .orchestratorClass(orchestratorClass)
        .instance(orchestratorBean)
        .stepCount(steps.size())
        .registeredAt(System.currentTimeMillis())
        .build();

    orchestratorMetadata.put(orchestratorName, metadata);
    totalSteps += steps.size();

    log.debug("Registered saga orchestrator '{}' with {} steps", orchestratorName, steps.size());
  }

  /**
   * Discover saga steps in an orchestrator class
   */
  private List<SagaStepInfo> discoverSteps(Class<?> orchestratorClass,
      Object orchestratorInstance) {
    List<SagaStepInfo> steps = new ArrayList<>();
    Method[] methods = orchestratorClass.getDeclaredMethods();

    for (Method method : methods) {
      SagaStep stepAnnotation = method.getAnnotation(SagaStep.class);
      if (stepAnnotation == null) {
        continue;
      }

      // Validate step method
      validateStepMethod(method, stepAnnotation);

      String stepName = stepAnnotation.name().isEmpty() ? method.getName() : stepAnnotation.name();
      String stepKey = generateStepKey(orchestratorClass.getSimpleName(), stepName);

      // Cache step method for performance
      stepMethodCache.put(stepKey, method);

      // Cache compensation method if specified
      if (!stepAnnotation.compensation().isEmpty()) {
        Method compensationMethod = findCompensationMethod(orchestratorClass,
            stepAnnotation.compensation());
        if (compensationMethod != null) {
          String compensationKey = generateCompensationKey(orchestratorClass.getSimpleName(),
              stepName);
          compensationMethodCache.put(compensationKey, compensationMethod);
        }
      }

      // Create step info
      SagaStepInfo stepInfo = SagaStepInfo.builder()
          .name(stepName)
          .order(stepAnnotation.order())
          .method(method)
          .compensation(stepAnnotation.compensation())
          .required(stepAnnotation.required())
          .timeoutMs(stepAnnotation.timeoutMs())
          .maxRetries(stepAnnotation.maxRetries())
          .retryDelayMs(stepAnnotation.retryDelayMs())
          .description(stepAnnotation.description())
          .orchestratorInstance(orchestratorInstance)
          .build();

      steps.add(stepInfo);
    }

    // Sort steps by order
    steps.sort(Comparator.comparing(SagaStepInfo::getOrder));

    // Validate step ordering
    validateStepOrdering(steps);

    return steps;
  }

  /**
   * Validate step method signature and annotations
   */
  private void validateStepMethod(Method method, SagaStep stepAnnotation) {
    // Check method accessibility
    if (!method.isAccessible()) {
      method.setAccessible(true);
    }

    // Validate timeout
    if (stepAnnotation.timeoutMs() < 0) {
      throw new IllegalArgumentException(
          String.format("Step timeout cannot be negative: %s.%s",
              method.getDeclaringClass().getSimpleName(), method.getName()));
    }

    // Validate max retries
    if (stepAnnotation.maxRetries() < 0) {
      throw new IllegalArgumentException(
          String.format("Step max retries cannot be negative: %s.%s",
              method.getDeclaringClass().getSimpleName(), method.getName()));
    }

    // Validate retry delay
    if (stepAnnotation.retryDelayMs() < 0) {
      throw new IllegalArgumentException(
          String.format("Step retry delay cannot be negative: %s.%s",
              method.getDeclaringClass().getSimpleName(), method.getName()));
    }
  }

  /**
   * Find compensation method by name
   */
  private Method findCompensationMethod(Class<?> orchestratorClass, String compensationMethodName) {
    try {
      Method[] methods = orchestratorClass.getDeclaredMethods();
      for (Method method : methods) {
        if (method.getName().equals(compensationMethodName)) {
          if (!method.isAccessible()) {
            method.setAccessible(true);
          }
          return method;
        }
      }

      log.warn("Compensation method '{}' not found in class {}",
          compensationMethodName, orchestratorClass.getSimpleName());
      return null;

    } catch (Exception e) {
      log.error("Error finding compensation method '{}' in class {}",
          compensationMethodName, orchestratorClass.getSimpleName(), e);
      return null;
    }
  }

  /**
   * Validate step ordering within an orchestrator
   */
  private void validateStepOrdering(List<SagaStepInfo> steps) {
    Set<Integer> orders = new HashSet<>();

    for (SagaStepInfo step : steps) {
      if (orders.contains(step.getOrder())) {
        throw new IllegalArgumentException(
            String.format("Duplicate step order %d found in orchestrator", step.getOrder()));
      }
      orders.add(step.getOrder());
    }
  }

  /**
   * Validate all registrations
   */
  private void validateRegistrations() {
    for (Map.Entry<String, List<SagaStepInfo>> entry : orchestratorSteps.entrySet()) {
      String orchestratorName = entry.getKey();
      List<SagaStepInfo> steps = entry.getValue();

      if (steps.isEmpty()) {
        log.warn("Orchestrator '{}' has no saga steps defined", orchestratorName);
      }

      // Validate step dependencies and compensation chains
      validateStepDependencies(orchestratorName, steps);
    }
  }

  /**
   * Validate step dependencies and compensation chains
   */
  private void validateStepDependencies(String orchestratorName, List<SagaStepInfo> steps) {
    for (SagaStepInfo step : steps) {
      // Validate compensation method exists if specified
      if (!step.getCompensation().isEmpty()) {
        String compensationKey = generateCompensationKey(orchestratorName, step.getName());
        if (!compensationMethodCache.containsKey(compensationKey)) {
          log.warn("Compensation method '{}' not found for step '{}' in orchestrator '{}'",
              step.getCompensation(), step.getName(), orchestratorName);
        }
      }
    }
  }

  /**
   * Get orchestrator instance by name
   */
  public Object getOrchestrator(String orchestratorName) {
    Object instance = orchestratorInstances.get(orchestratorName);
    if (instance == null) {
      throw new IllegalArgumentException("Saga orchestrator not found: " + orchestratorName);
    }
    return instance;
  }

  /**
   * Get compensation method from cache
   */
  public Method getCompensationMethod(String orchestratorName, String stepName) {
    String compensationKey = generateCompensationKey(orchestratorName, stepName);
    return compensationMethodCache.get(compensationKey);
  }

  /**
   * Generate cache key for step method
   */
  private String generateStepKey(String orchestratorName, String stepName) {
    return orchestratorName + "." + stepName;
  }

  /**
   * Generate cache key for compensation method
   */
  private String generateCompensationKey(String orchestratorName, String stepName) {
    return orchestratorName + "." + stepName + ".compensation";
  }

  /**
   * Saga step information holder
   */
  @Data
  @Builder
  public static class SagaStepInfo {

    private final String name;
    private final int order;
    private final Method method;
    private final String compensation;
    private final boolean required;
    private final long timeoutMs;
    private final int maxRetries;
    private final long retryDelayMs;
    private final String description;
    private final Object orchestratorInstance;
  }

  /**
   * Saga orchestrator information holder
   */
  @Data
  @Builder
  public static class SagaOrchestratorInfo {

    private final String name;
    private final String beanName;
    private final Class<?> orchestratorClass;
    private final Object instance;
    private final int stepCount;
    private final long registeredAt;
  }

  /**
   * Registry statistics holder
   */
  @Data
  @Builder
  public static class RegistryStatistics {

    private final int totalOrchestrators;
    private final int totalSteps;
    private final long lastRegistrationTime;
    private final int cacheSize;
  }
}
