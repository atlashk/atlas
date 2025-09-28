package org.atlas.framework.saga.orchestrator;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.entity.SagaStepEntity;
import org.atlas.framework.saga.entity.SagaStepStatus;
import org.atlas.framework.saga.annotation.SagaStep;
import org.atlas.framework.saga.event.SagaEventPublisher;
import org.atlas.framework.saga.repository.SagaRepository;
import org.atlas.framework.saga.repository.SagaStepRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Comprehensive error handler for saga operations with retry mechanisms
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SagaErrorHandler {

  private final SagaRepository sagaRepository;
  private final SagaStepRepository stepRepository;
  private final SagaEventPublisher eventPublisher;
  private final SagaCompensationManager compensationManager;
  private final SagaTransactionManager transactionManager;
  private final ScheduledExecutorService retryScheduler = Executors.newScheduledThreadPool(5);

  /**
   * Handle saga execution error
   */
  @Transactional
  public void handleSagaError(Long sagaId, String orchestratorName, Throwable error) {
    log.error("Handling saga error for saga: {} with orchestrator: {}", sagaId, orchestratorName,
        error);

    try {
      // Update saga status and error message
      updateSagaError(sagaId, error.getMessage());

      // Rollback transaction if active
      if (transactionManager.hasActiveTransaction(sagaId)) {
        transactionManager.rollbackSagaTransaction(sagaId);
      }

      // Publish saga failed event
      eventPublisher.publishSagaFailedEvent(sagaId, error.getMessage());

      // Start compensation process
      compensationManager.startCompensation(sagaId, orchestratorName);

      log.info("Saga error handled successfully for saga: {}", sagaId);

    } catch (Exception e) {
      log.error("Error handling saga error for saga: {}", sagaId, e);
      // Mark saga as failed if compensation also fails
      markSagaAsFailed(sagaId, "Error handling failed: " + e.getMessage());
    }
  }

  /**
   * Handle step execution error with retry logic
   */
  @Transactional
  public void handleStepError(Long sagaId, Long stepId, String stepName, Throwable error) {
    log.error("Handling step error for step: {} in saga: {}", stepName, sagaId, error);

    try {
      Optional<SagaStepEntity> stepOpt = stepRepository.findById(stepId);
      if (stepOpt.isEmpty()) {
        log.error("Step not found for error handling: {}", stepId);
        return;
      }

      SagaStepEntity step = stepOpt.get();

      // Increment retry count
      int currentRetries = step.getRetryCount() != null ? step.getRetryCount() : 0;
      step.setRetryCount(currentRetries + 1);
      step.setErrorMessage(error.getMessage());

      // Check if we should retry
      SagaStep stepAnnotation = getStepAnnotation(sagaId, stepName);
      int maxRetries = stepAnnotation != null ? stepAnnotation.maxRetries() : 3;

      if (step.getRetryCount() < maxRetries) {
        // Schedule retry
        scheduleStepRetry(sagaId, stepId, stepName, stepAnnotation);

        // Update step status to retrying
        step.setStepStatus(SagaStepStatus.RETRYING);
        stepRepository.update(step);

        log.info("Scheduled retry {} of {} for step: {} in saga: {}",
            step.getRetryCount(), maxRetries, stepName, sagaId);

      } else {
        // Max retries exceeded, mark step as failed
        step.setStepStatus(SagaStepStatus.FAILED);
        step.setCompletedAt(LocalDateTime.now());
        stepRepository.update(step);

        // Rollback step transaction
        transactionManager.rollbackStepTransaction(sagaId, stepName);

        // Publish step failed event
        eventPublisher.publishStepFailedEvent(sagaId, stepName, error.getMessage());

        // Handle saga failure
        handleSagaError(sagaId, getOrchestratorName(sagaId),
            new RuntimeException("Step failed after " + maxRetries + " retries: " + stepName));

        log.error("Step failed after {} retries: {} in saga: {}", maxRetries, stepName, sagaId);
      }

    } catch (Exception e) {
      log.error("Error handling step error for step: {} in saga: {}", stepName, sagaId, e);
      markStepAsFailed(stepId, "Error handling failed: " + e.getMessage());
    }
  }

  /**
   * Handle timeout errors
   */
  @Transactional
  public void handleTimeoutError(Long sagaId, Long stepId, String stepName) {
    log.warn("Handling timeout error for step: {} in saga: {}", stepName, sagaId);

    try {
      // Treat timeout as a regular error but with specific message
      handleStepError(sagaId, stepId, stepName,
          new RuntimeException("Step execution timeout: " + stepName));

    } catch (Exception e) {
      log.error("Error handling timeout for step: {} in saga: {}", stepName, sagaId, e);
    }
  }

  /**
   * Handle compensation errors
   */
  @Transactional
  public void handleCompensationError(Long sagaId, Long stepId, String compensationName,
      Throwable error) {
    log.error("Handling compensation error for: {} in saga: {}", compensationName, sagaId, error);

    try {
      // Let compensation manager handle the retry logic
      // This is just for additional logging and monitoring

      // Update saga status if all compensations fail
      if (compensationManager.hasFailedCompensations(sagaId)) {
        updateSagaStatus(sagaId, SagaStatus.COMPENSATION_FAILED);
      }

    } catch (Exception e) {
      log.error("Error handling compensation error for: {} in saga: {}", compensationName, sagaId,
          e);
    }
  }

  /**
   * Retry failed sagas
   */
  public void retryFailedSagas() {
    log.debug("Checking for failed sagas to retry");

    try {
      List<SagaEntity> failedSagas = sagaRepository.findByStatus(SagaStatus.FAILED);

      for (SagaEntity saga : failedSagas) {
        // Check if saga is eligible for retry (e.g., not too old, not exceeded max retries)
        if (isSagaEligibleForRetry(saga)) {
          log.info("Retrying failed saga: {}", saga.getSagaId());
          retryFailedSaga(saga);
        }
      }

    } catch (Exception e) {
      log.error("Error retrying failed sagas", e);
    }
  }

  /**
   * Retry failed steps
   */
  public void retryFailedSteps() {
    log.debug("Checking for failed steps to retry");

    try {
      List<SagaStepEntity> failedSteps = stepRepository.findByStatus(SagaStepStatus.FAILED);

      for (SagaStepEntity step : failedSteps) {
        // Check if step is eligible for retry
        if (isStepEligibleForRetry(step)) {
          log.info("Retrying failed step: {} in saga: {}", step.getStepName(), step.getSagaId());
          retryFailedStep(step);
        }
      }

    } catch (Exception e) {
      log.error("Error retrying failed steps", e);
    }
  }

  /**
   * Clean up old failed sagas and steps
   */
  public void cleanupOldFailures() {
    log.debug("Cleaning up old failed sagas and steps");

    try {
      LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // Keep failures for 30 days

      // Clean up old failed sagas
      List<SagaEntity> oldFailedSagas = sagaRepository.findByStatus(SagaStatus.FAILED);
      for (SagaEntity saga : oldFailedSagas) {
        if (saga.getStartedAt() != null && saga.getStartedAt().isBefore(cutoffDate)) {
          log.info("Cleaning up old failed saga: {}", saga.getSagaId());
          // Archive or delete old saga data
          archiveFailedSaga(saga);
        }
      }

    } catch (Exception e) {
      log.error("Error cleaning up old failures", e);
    }
  }

  // Helper methods

  private void scheduleStepRetry(Long sagaId, Long stepId, String stepName,
      SagaStep stepAnnotation) {
    long retryDelayMs =
        stepAnnotation != null ? stepAnnotation.retryDelayMs() : 5000; // Default 5 seconds

    retryScheduler.schedule(() -> {
      try {
        log.info("Executing scheduled retry for step: {} in saga: {}", stepName, sagaId);

        // Reset step status for retry
        Optional<SagaStepEntity> stepOpt = stepRepository.findById(stepId);
        if (stepOpt.isPresent()) {
          SagaStepEntity step = stepOpt.get();
          step.setStepStatus(SagaStepStatus.PENDING);
          step.setStartedAt(null);
          step.setCompletedAt(null);
          stepRepository.update(step);

          // Trigger step execution (this would typically be done by SagaManager)
          // For now, just log that retry is ready
          log.info("Step ready for retry: {} in saga: {}", stepName, sagaId);
        }

      } catch (Exception e) {
        log.error("Error executing scheduled retry for step: {} in saga: {}", stepName, sagaId, e);
      }
    }, retryDelayMs, TimeUnit.MILLISECONDS);
  }

  private SagaStep getStepAnnotation(Long sagaId, String stepName) {
    // This would typically involve looking up the orchestrator and finding the step method
    // For now, return null and use defaults
    return null;
  }

  private String getOrchestratorName(Long sagaId) {
    try {
      Optional<SagaEntity> sagaOpt = sagaRepository.findById(sagaId);
      return sagaOpt.map(SagaEntity::getOrchestratorName).orElse("unknown");
    } catch (Exception e) {
      log.error("Error getting orchestrator name for saga: {}", sagaId, e);
      return "unknown";
    }
  }

  private void updateSagaError(Long sagaId, String errorMessage) {
    try {
      Optional<SagaEntity> sagaOpt = sagaRepository.findById(sagaId);
      if (sagaOpt.isPresent()) {
        SagaEntity saga = sagaOpt.get();
        saga.setSagaStatus(SagaStatus.FAILED);
        saga.setErrorMessage(errorMessage);
        saga.setCompletedAt(LocalDateTime.now());
        sagaRepository.update(saga);
      }
    } catch (Exception e) {
      log.error("Error updating saga error for saga: {}", sagaId, e);
    }
  }

  private void updateSagaStatus(Long sagaId, SagaStatus status) {
    try {
      Optional<SagaEntity> sagaOpt = sagaRepository.findById(sagaId);
      if (sagaOpt.isPresent()) {
        SagaEntity saga = sagaOpt.get();
        saga.setSagaStatus(status);
        sagaRepository.update(saga);
      }
    } catch (Exception e) {
      log.error("Error updating saga status for saga: {}", sagaId, e);
    }
  }

  private void markSagaAsFailed(Long sagaId, String errorMessage) {
    try {
      updateSagaError(sagaId, errorMessage);
      log.error("Marked saga as failed: {} with error: {}", sagaId, errorMessage);
    } catch (Exception e) {
      log.error("Error marking saga as failed: {}", sagaId, e);
    }
  }

  private void markStepAsFailed(Long stepId, String errorMessage) {
    try {
      Optional<SagaStepEntity> stepOpt = stepRepository.findById(stepId);
      if (stepOpt.isPresent()) {
        SagaStepEntity step = stepOpt.get();
        step.setStepStatus(SagaStepStatus.FAILED);
        step.setErrorMessage(errorMessage);
        step.setCompletedAt(LocalDateTime.now());
        stepRepository.update(step);
      }
    } catch (Exception e) {
      log.error("Error marking step as failed: {}", stepId, e);
    }
  }

  private boolean isSagaEligibleForRetry(SagaEntity saga) {
    // Check if saga is not too old and hasn't exceeded max retries
    LocalDateTime cutoffDate = LocalDateTime.now()
        .minusHours(24); // Don't retry sagas older than 24 hours
    return saga.getStartedAt() != null && saga.getStartedAt().isAfter(cutoffDate);
  }

  private boolean isStepEligibleForRetry(SagaStepEntity step) {
    // Check if step is not too old and hasn't exceeded max retries
    LocalDateTime cutoffDate = LocalDateTime.now()
        .minusHours(6); // Don't retry steps older than 6 hours
    return step.getStartedAt() != null && step.getStartedAt().isAfter(cutoffDate) &&
        (step.getRetryCount() == null || step.getRetryCount() < 3);
  }

  private void retryFailedSaga(SagaEntity saga) {
    // Reset saga status and trigger restart
    saga.setSagaStatus(SagaStatus.PENDING);
    saga.setErrorMessage(null);
    sagaRepository.update(saga);

    log.info("Reset failed saga for retry: {}", saga.getSagaId());
  }

  private void retryFailedStep(SagaStepEntity step) {
    // Reset step status for retry
    step.setStepStatus(SagaStepStatus.PENDING);
    step.setErrorMessage(null);
    step.setStartedAt(null);
    step.setCompletedAt(null);
    stepRepository.update(step);

    log.info("Reset failed step for retry: {} in saga: {}", step.getStepName(), step.getSagaId());
  }

  private void archiveFailedSaga(SagaEntity saga) {
    // In a real implementation, you might move data to an archive table
    // For now, just log the archival
    log.info("Archived old failed saga: {}", saga.getSagaId());
  }

  // Public API methods

  public CompletableFuture<Void> handleErrorAsync(Long sagaId, String orchestratorName,
      Throwable error) {
    return CompletableFuture.runAsync(() -> handleSagaError(sagaId, orchestratorName, error));
  }

  public CompletableFuture<Void> handleStepErrorAsync(Long sagaId, Long stepId, String stepName,
      Throwable error) {
    return CompletableFuture.runAsync(() -> handleStepError(sagaId, stepId, stepName, error));
  }

  public void shutdown() {
    retryScheduler.shutdown();
    try {
      if (!retryScheduler.awaitTermination(60, TimeUnit.SECONDS)) {
        retryScheduler.shutdownNow();
      }
    } catch (InterruptedException e) {
      retryScheduler.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}