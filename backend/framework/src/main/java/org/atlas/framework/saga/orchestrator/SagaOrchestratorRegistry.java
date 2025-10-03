package org.atlas.framework.saga.orchestrator;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.annotation.SagaOrchestrator;
import org.atlas.framework.saga.exception.SagaConfigException;
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

  // Thread-safe caches for saga orchestrator metadata
  private final Map<String, SagaOrchestratorMetadata> orchestrators = new ConcurrentHashMap<>();

  /**
   * Initialize the registry by discovering and registering all saga orchestrators
   */
  @Override
  public void afterPropertiesSet() throws Exception {
    registerOrchestrators();
    log.info("Registered {} saga orchestrators successfully", orchestrators.size());
  }

  public Optional<SagaOrchestratorMetadata> getOrchestrator(String sagaName) {
    return Optional.ofNullable(orchestrators.get(sagaName));
  }

  public boolean hasOrchestrator(String sagaName) {
    return orchestrators.containsKey(sagaName);
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

    String sagaName = sagaOrchestratorAnnotation.sagaName();

    // Check for duplicate orchestrator names
    if (orchestrators.containsKey(sagaName)) {
      throw new SagaConfigException("Duplicate saga orchestrator name: " + sagaName);
    }

    // Register orchestrator instance
    SagaOrchestratorMetadata.SagaOrchestratorMetadataBuilder sagaOrchestratorMetadataBuilder =
        SagaOrchestratorMetadata.builder()
            .sagaName(sagaName)
            .orchestratorInstance(orchestratorBean);

    orchestrators.put(sagaName, sagaOrchestratorMetadataBuilder.build());

    log.debug("Registered orchestrator for saga {}", sagaName);
  }
}
