package org.atlas.framework.saga.orchestrator;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaOrchestrator;
import org.atlas.framework.saga.annotation.SagaStep;
import org.atlas.framework.saga.exception.SagaConfigException;
import org.atlas.framework.util.StringUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Enhanced registry for saga orchestrators and their steps. Provides efficient caching and
 * validation of saga configurations.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaOrchestratorRegistry implements InitializingBean {

  private final ApplicationContext applicationContext;

  // Thread-safe caches for orchestrator metadata
  private final Map<String, SagaOrchestratorMetadata> orchestrators = new ConcurrentHashMap<>();

  /**
   * Initialize the registry by discovering and registering all saga orchestrators
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    registerOrchestrators();
    log.info("Registered {} saga orchestrators successfully", orchestrators.size());
  }

  public boolean hasOrchestrator(String name) {
    return orchestrators.containsKey(name);
  }

  @Nonnull
  public SagaStepMetadata getFirstStep(String orchestratorName) {
    SagaOrchestratorMetadata metadata = orchestrators.get(orchestratorName);
    if (metadata == null || metadata.getSteps().isEmpty()) {
      throw new SagaConfigException("No steps found for orchestrator: " + orchestratorName);
    }
    return metadata.getSteps().get(0);
  }

  @Nonnull
  public SagaStepMetadata getStep(String orchestratorName, String stepName) {
    SagaOrchestratorMetadata metadata = orchestrators.get(orchestratorName);
    if (metadata == null || metadata.getSteps().isEmpty()) {
      throw new SagaConfigException("No steps found for orchestrator: " + orchestratorName);
    }
    return metadata.getSteps().stream()
        .filter(step -> step.getStepName().equals(stepName))
        .findFirst()
        .orElseThrow(() -> new SagaConfigException(
            String.format("Step %s not found in orchestrator %s", stepName, orchestratorName)));
  }

  @Nullable
  public SagaStepMetadata getNextStep(String orchestratorName, String currentStepName) {
    SagaOrchestratorMetadata metadata = orchestrators.get(orchestratorName);
    if (metadata == null || metadata.getSteps().isEmpty()) {
      throw new SagaConfigException("No steps found for orchestrator: " + orchestratorName);
    }
    List<SagaStepMetadata> steps = metadata.getSteps();
    for (int i = 0; i < steps.size() - 1; i++) {
      if (steps.get(i).getStepName().equals(currentStepName)) {
        return steps.get(i + 1);
      }
    }
    return null; // No next step
  }

  /**
   * Register all saga orchestrators from the application context
   */
  private void registerOrchestrators() {
    Map<String, Object> orchestratorBeans = applicationContext.getBeansWithAnnotation(
        SagaOrchestrator.class);
    log.debug("Found {} potential saga orchestrator beans", orchestratorBeans.size());

    for (Map.Entry<String, Object> entry : orchestratorBeans.entrySet()) {
      String orchestratorBeanName = entry.getKey();
      Object orchestratorBean = entry.getValue();
      try {
        registerOrchestrator(orchestratorBean);
      } catch (Exception e) {
        throw new SagaConfigException(
            String.format("Failed to register saga orchestrator for bean %s: %s",
                orchestratorBeanName, e.getMessage()), e);
      }
    }
  }

  /**
   * Register a single orchestrator and its steps
   */
  private void registerOrchestrator(Object orchestratorBean) {
    Class<?> orchestratorClass = orchestratorBean.getClass();
    SagaOrchestrator sagaOrchestratorAnnotation = orchestratorClass.getAnnotation(
        SagaOrchestrator.class);

    String orchestratorName = sagaOrchestratorAnnotation.name();

    // Check for duplicate orchestrator names
    if (orchestrators.containsKey(orchestratorName)) {
      throw new SagaConfigException("Duplicate saga orchestrator name: " + orchestratorName);
    }

    // Register orchestrator instance
    SagaOrchestratorMetadata.SagaOrchestratorMetadataBuilder sagaOrchestratorMetadataBuilder =
        SagaOrchestratorMetadata.builder()
            .orchestratorName(orchestratorName)
            .orchestratorInstance(orchestratorBean)
            .steps(obtainSteps(orchestratorBean));

    // Register succeeded and failed handlers if specified
    if (StringUtil.isNotBlank(sagaOrchestratorAnnotation.completionHandler())) {
      sagaOrchestratorMetadataBuilder.sagaCompletionHandler(
          obtainMethod(orchestratorBean, sagaOrchestratorAnnotation.completionHandler()));
    }
    if (StringUtil.isNotBlank(sagaOrchestratorAnnotation.failureHandler())) {
      sagaOrchestratorMetadataBuilder.sagaFailureHandler(
          obtainMethod(orchestratorBean, sagaOrchestratorAnnotation.failureHandler()));
    }

    orchestrators.put(orchestratorName, sagaOrchestratorMetadataBuilder.build());

    log.debug("Registered saga orchestrator '{}'", orchestratorName);
  }

  /**
   * Register saga steps in an orchestrator class
   */
  private List<SagaStepMetadata> obtainSteps(Object orchestratorInstance) {
    List<SagaStepMetadata> stepMetadataList = new ArrayList<>();
    Method[] methods = orchestratorInstance.getClass().getDeclaredMethods();

    for (Method method : methods) {
      SagaStep stepAnnotation = method.getAnnotation(SagaStep.class);
      if (stepAnnotation == null) {
        continue;
      }

      // Validate step method
      if (!method.canAccess(orchestratorInstance)) {
        throw new SagaConfigException(
            String.format(
                "Method %s should be accessible in the orchestrator class %s",
                method.getName(), orchestratorInstance.getClass().getSimpleName()));
      }

      SagaStepMetadata.SagaStepMetadataBuilder stepMetadataBuilder = SagaStepMetadata.builder()
          .orchestratorInstance(orchestratorInstance)
          .stepName(stepAnnotation.name())
          .stepOrder(stepAnnotation.order())
          .stepMethod(method);

      // Obtain compensation method if specified
      if (!stepAnnotation.compensation().isEmpty()) {
        Method compensationMethod = obtainMethod(orchestratorInstance,
            stepAnnotation.compensation());
        if (compensationMethod != null) {
          stepMetadataBuilder.compensateMethod(compensationMethod);
        }
      }

      stepMetadataList.add(stepMetadataBuilder.build());
    }

    // Sort steps by order
    stepMetadataList.sort(Comparator.comparing(SagaStepMetadata::getStepOrder));

    // Validate step ordering
    validateStepOrdering(orchestratorInstance, stepMetadataList);

    return stepMetadataList;
  }

  /**
   * Obtain the compensation method by name
   */
  private Method obtainMethod(Object orchestratorInstance,
      String compensationMethodName) {
    Method[] methods = orchestratorInstance.getClass().getDeclaredMethods();
    for (Method method : methods) {
      if (method.getName().equals(compensationMethodName)) {
        if (!method.canAccess(orchestratorInstance)) {
          throw new SagaConfigException(
              String.format(
                  "Method %s should be accessible in the orchestrator class %s",
                  method.getName(), orchestratorInstance.getClass().getSimpleName()));
        }
        return method;
      }
    }
    log.warn("Method {} not found in the orchestrator class {}",
        compensationMethodName, orchestratorInstance.getClass().getSimpleName());
    return null;
  }

  /**
   * Validate step ordering within an orchestrator
   */
  private void validateStepOrdering(Object orchestratorInstance, List<SagaStepMetadata> steps) {
    Set<Integer> orders = new HashSet<>();
    for (SagaStepMetadata step : steps) {
      if (orders.contains(step.getStepOrder())) {
        throw new SagaConfigException(
            String.format("Duplicate step order %d found in orchestrator %s",
                step.getStepOrder(), orchestratorInstance.getClass().getSimpleName()));
      }
      orders.add(step.getStepOrder());
    }
  }
}
