package org.atlas.framework.saga.orchestrator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.entity.SagaStepEntity;
import org.atlas.framework.saga.entity.SagaStepStatus;
import org.atlas.framework.saga.event.SagaEventPublisher;
import org.atlas.framework.saga.repository.SagaCompensationRepository;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.framework.saga.repository.SagaStepRepository;
import org.atlas.framework.saga.util.SagaUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Event-driven Saga Manager responsible for orchestrating saga execution lifecycle. Uses pure
 * event-driven approach without maintaining in-memory state.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SagaManager {

  private final SagaOrchestratorRegistry orchestratorRegistry;
  private final SagaEventPublisher eventPublisher;
  private final SagaRepository sagaRepository;
  private final SagaStepRepository sagaStepRepository;
  private final SagaCompensationRepository sagaCompensationRepository;
  private final SagaCompensationManager compensationManager;
  private final Executor sagaExecutor;

  /**
   * Start a new saga with comprehensive validation and error handling
   */
  @Transactional
  public CompletableFuture<String> startSaga(String orchestratorName,
      Map<String, Object> sagaData) {

    return CompletableFuture.supplyAsync(() -> {
      try {
        log.info("Starting saga with orchestrator: {}", orchestratorName);

        // Validate orchestrator exists
        if (!orchestratorRegistry.hasOrchestrator(orchestratorName)) {
          throw new IllegalArgumentException("Unknown orchestrator: " + orchestratorName);
        }

        // Validate saga data
        validateSagaData(sagaData);

        // Create saga entity
        SagaEntity saga = createSagaEntity(orchestratorName, sagaData);
        saga = sagaRepository.save(saga);

        // Initialize saga steps
        initializeSagaSteps(saga);

        // Update saga status to started
        saga.setStatus(SagaStatus.STARTED);
        saga.setStartedAt(LocalDateTime.now());
        sagaRepository.save(saga);

        // Publish saga started event
        eventPublisher.publishSagaStarted(saga.getSagaId(), orchestratorName, sagaData);

        log.info("Saga started successfully: {}", saga.getSagaId());
        return saga.getSagaId();

      } catch (Exception e) {
        log.error("Failed to start saga with orchestrator: {}", orchestratorName, e);
        throw new SagaExecutionException("Failed to start saga", e);
      }
    }, sagaExecutor);
  }

  /**
   * Execute the next step in the saga with enhanced error handling
   */
  @Transactional
  public CompletableFuture<Void> executeNextStep(String sagaId) {
    return CompletableFuture.runAsync(() -> {
      try {
        log.debug("Executing next step for saga: {}", sagaId);

        // Get current saga state
        SagaEntity saga = sagaRepository.findById(sagaId)
            .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

        // Validate saga can execute steps
        if (!saga.getStatus().allowsStepExecution()) {
          log.warn("Saga {} is in status {} and cannot execute steps", sagaId, saga.getStatus());
          return;
        }

        // Find next step to execute
        Optional<SagaStepEntity> nextStep = findNextStepToExecute(sagaId);
        if (nextStep.isEmpty()) {
          // No more steps to execute, complete saga
          completeSaga(sagaId);
          return;
        }

        // Execute the step
        executeStep(saga, nextStep.get());

      } catch (Exception e) {
        log.error("Failed to execute next step for saga: {}", sagaId, e);
        handleSagaFailure(sagaId, e);
      }
    }, sagaExecutor);
  }

  /**
   * Complete saga execution with proper cleanup
   */
  @Transactional
  public void completeSaga(@NotBlank String sagaId) {
    try {
      log.info("Completing saga: {}", sagaId);

      SagaEntity saga = sagaRepository.findById(sagaId)
          .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

      // Validate all steps are completed
      List<SagaStepEntity> incompleteSteps = sagaStepRepository.findBySagaIdAndStatusNotIn(
          sagaId, Set.of(SagaStepStatus.COMPLETED, SagaStepStatus.SKIPPED));

      if (!incompleteSteps.isEmpty()) {
        log.warn("Attempting to complete saga {} with incomplete steps: {}",
            sagaId,
            incompleteSteps.stream().map(SagaStepEntity::getStepName).collect(Collectors.toList()));
        return;
      }

      // Mark saga as completed
      saga.markCompleted();
      sagaRepository.save(saga);

      // Publish saga completed event
      eventPublisher.publishSagaCompleted(sagaId, saga.getOrchestratorName());

      log.info("Saga completed successfully: {}", sagaId);

    } catch (Exception e) {
      log.error("Failed to complete saga: {}", sagaId, e);
      throw new SagaExecutionException("Failed to complete saga", e);
    }
  }

  /**
   * Handle saga failure with comprehensive error management
   */
  @Transactional
  public void handleSagaFailure(@NotBlank String sagaId, @NotNull Throwable error) {
    try {
      log.error("Handling saga failure for: {}", sagaId, error);

      SagaEntity saga = sagaRepository.findById(sagaId)
          .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

      // Mark saga as failed
      saga.markFailed();
      saga.setErrorMessage(SagaUtil.createErrorMessage(error));
      sagaRepository.save(saga);

      // Publish saga failed event
      eventPublisher.publishSagaFailed(sagaId, saga.getOrchestratorName(), error);

      // Start compensation if needed
      if (shouldStartCompensation(sagaId)) {
        compensationManager.startCompensation(sagaId);
      }

    } catch (Exception e) {
      log.error("Failed to handle saga failure for: {}", sagaId, e);
      // Don't throw here to avoid cascading failures
    }
  }

  /**
   * Handle step failure with retry logic and compensation
   */
  @Transactional
  public void handleStepFailure(@NotBlank String sagaId, @NotBlank String stepName,
      @NotNull Throwable error) {
    try {
      log.error("Handling step failure for saga: {}, step: {}", sagaId, stepName, error);

      SagaStepEntity step = sagaStepRepository.findBySagaIdAndStepName(sagaId, stepName)
          .orElseThrow(() -> new SagaStepNotFoundException("Step not found: " + stepName));

      SagaEntity saga = sagaRepository.findById(sagaId)
          .orElseThrow(() -> new SagaNotFoundException("Saga not found: " + sagaId));

      // Check if step can be retried
      if (step.canRetry()) {
        log.info("Retrying step: {} for saga: {}", stepName, sagaId);
        step.startRetry();
        step.setErrorMessage(SagaUtil.createErrorMessage(error));
        sagaStepRepository.save(step);

        // Schedule retry execution
        scheduleStepRetry(sagaId, stepName);
        return;
      }

      // Mark step as failed
      step.markFailed();
      step.setErrorMessage(SagaUtil.createErrorMessage(error));
      sagaStepRepository.save(step);

      // Publish step failed event
      eventPublisher.publishStepFailed(sagaId, saga.getOrchestratorName(), stepName, error);

      // Handle saga failure
      handleSagaFailure(sagaId, error);

    } catch (Exception e) {
      log.error("Failed to handle step failure for saga: {}, step: {}", sagaId, stepName, e);
      handleSagaFailure(sagaId, e);
    }
  }

  /**
   * Get saga statistics
   */
  public SagaStatistics getSagaStatistics() {
    return SagaStatistics.builder()
        .totalSagasCount(sagaRepository.count())
        .completedSagasCount(sagaRepository.countByStatus(SagaStatus.COMPLETED))
        .failedSagasCount(sagaRepository.countByStatus(SagaStatus.FAILED))
        .compensatedSagasCount(sagaRepository.countByStatus(SagaStatus.COMPENSATED))
        .activeSagasCount(sagaRepository.countByStatusIn(
            Set.of(SagaStatus.STARTED, SagaStatus.IN_PROGRESS)))
        .build();
  }

  /**
   * Get saga details by ID
   */
  public Optional<SagaEntity> getSagaById(@NotBlank String sagaId) {
    return sagaRepository.findById(sagaId);
  }

  /**
   * Get saga steps by saga ID
   */
  public List<SagaStepEntity> getSagaSteps(@NotBlank String sagaId) {
    return sagaStepRepository.findBySagaId(sagaId);
  }

  // Private helper methods

  private void validateSagaData(Map<String, Object> sagaData) {
    if (sagaData == null) {
      throw new IllegalArgumentException("Saga data cannot be null");
    }
    // Additional validation can be added here
  }

  private SagaEntity createSagaEntity(String orchestratorName, Map<String, Object> sagaData) {
    return SagaEntity.builder()
        .sagaId(SagaUtil.generateSagaId())
        .sagaName(SagaUtil.generateSagaName(orchestratorName))
        .orchestratorName(orchestratorName)
        .status(SagaStatus.PENDING)
        .sagaData(new HashMap<>(sagaData))
        .createdAt(LocalDateTime.now())
        .build();
  }

  private void initializeSagaSteps(SagaEntity saga) {
    List<String> stepNames = orchestratorRegistry.getStepNames(saga.getOrchestratorName());
    List<SagaStepEntity> steps = new ArrayList<>();

    for (int i = 0; i < stepNames.size(); i++) {
      String stepName = stepNames.get(i);
      SagaStepEntity step = SagaStepEntity.builder()
          .stepId(SagaUtil.generateStepId())
          .sagaId(saga.getSagaId())
          .stepName(stepName)
          .stepOrder(i + 1)
          .status(SagaStepStatus.PENDING)
          .maxRetries(SagaUtil.getStepMaxRetries(saga.getOrchestratorName(), stepName))
          .retryCount(0)
          .timeoutMs(SagaUtil.getStepTimeoutMs(saga.getOrchestratorName(), stepName))
          .required(SagaUtil.isStepRequired(saga.getOrchestratorName(), stepName))
          .createdAt(LocalDateTime.now())
          .build();
      steps.add(step);
    }

    sagaStepRepository.saveAll(steps);
  }

  private Optional<SagaStepEntity> findNextStepToExecute(String sagaId) {
    return sagaStepRepository.findBySagaIdAndStatus(sagaId, SagaStepStatus.PENDING)
        .stream()
        .min(Comparator.comparing(SagaStepEntity::getStepOrder));
  }

  private void executeStep(SagaEntity saga, SagaStepEntity step) {
    try {
      log.debug("Executing step: {} for saga: {}", step.getStepName(), saga.getSagaId());

      // Mark step as in progress
      step.start();
      sagaStepRepository.save(step);

      // Publish step started event
      eventPublisher.publishStepStarted(saga.getSagaId(), saga.getOrchestratorName(),
          step.getStepName());

      // Execute the actual step logic
      boolean stepResult = orchestratorRegistry.executeStep(
          saga.getOrchestratorName(),
          step.getStepName(),
          saga.getSagaData());

      if (stepResult) {
        // Mark step as completed
        step.markCompleted();
        sagaStepRepository.save(step);

        // Publish step completed event
        eventPublisher.publishStepCompleted(saga.getSagaId(), saga.getOrchestratorName(),
            step.getStepName());

        // Continue with next step
        executeNextStep(saga.getSagaId());

      } else {
        throw new SagaStepExecutionException(
            "Step execution returned false: " + step.getStepName());
      }

    } catch (Exception e) {
      log.error("Step execution failed: {} for saga: {}", step.getStepName(), saga.getSagaId(), e);
      handleStepFailure(saga.getSagaId(), step.getStepName(), e);
    }
  }

  private boolean shouldStartCompensation(String sagaId) {
    List<SagaStepEntity> completedSteps = sagaStepRepository.findBySagaIdAndStatus(sagaId,
        SagaStepStatus.COMPLETED);
    return !completedSteps.isEmpty();
  }

  private void scheduleStepRetry(String sagaId, String stepName) {
    // Schedule retry after delay (implementation would depend on scheduling mechanism)
    CompletableFuture.runAsync(() -> {
      try {
        Thread.sleep(1000); // Simple delay, should be configurable
        executeNextStep(sagaId);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        log.warn("Step retry scheduling interrupted for saga: {}, step: {}", sagaId, stepName);
      }
    }, sagaExecutor);
  }

  // Statistics class
  @Data
  @Builder
  public static class SagaStatistics {

    private final long activeSagasCount;
    private final long totalSagasCount;
    private final long completedSagasCount;
    private final long failedSagasCount;
    private final long compensatedSagasCount;
  }
}
