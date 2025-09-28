package org.atlas.framework.saga.event;

import org.atlas.framework.saga.entity.SagaEntity;
import org.atlas.framework.saga.entity.SagaStepEntity;
import org.atlas.framework.saga.entity.SagaCompensationEntity;
import org.atlas.framework.saga.entity.SagaStatus;
import org.atlas.framework.saga.entity.SagaStepStatus;
import org.atlas.framework.saga.entity.SagaCompensationStatus;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Enhanced interface for publishing saga-related events with comprehensive event types and support
 * for both synchronous and asynchronous event publishing.
 */
public interface SagaEventPublisher {

  // ========== Saga Lifecycle Events ==========

  /**
   * Publish saga started event
   */
  void publishSagaStarted(@NotBlank String sagaId,
      @NotBlank String orchestratorName,
      @NotNull Map<String, Object> sagaData);

  /**
   * Publish saga started event with entity
   */
  void publishSagaStarted(@NotNull SagaEntity saga);

  /**
   * Publish saga completed event
   */
  void publishSagaCompleted(@NotBlank String sagaId,
      @NotBlank String orchestratorName,
      long durationMs);

  /**
   * Publish saga completed event with entity
   */
  void publishSagaCompleted(@NotNull SagaEntity saga);

  /**
   * Publish saga failed event
   */
  void publishSagaFailed(@NotBlank String sagaId,
      @NotBlank String orchestratorName,
      @NotBlank String errorMessage,
      String failedStepName);

  /**
   * Publish saga failed event with entity
   */
  void publishSagaFailed(@NotNull SagaEntity saga, @NotBlank String errorMessage);

  /**
   * Publish saga cancelled event
   */
  void publishSagaCancelled(@NotBlank String sagaId,
      @NotBlank String orchestratorName,
      @NotBlank String reason);

  /**
   * Publish saga status changed event
   */
  void publishSagaStatusChanged(@NotBlank String sagaId,
      @NotNull SagaStatus oldStatus,
      @NotNull SagaStatus newStatus);

  // ========== Saga Step Events ==========

  /**
   * Publish step started event
   */
  void publishStepStarted(@NotBlank String sagaId,
      @NotBlank String stepName,
      int stepOrder,
      @NotNull Map<String, Object> stepData);

  /**
   * Publish step started event with entity
   */
  void publishStepStarted(@NotNull SagaStepEntity step);

  /**
   * Publish step completed event
   */
  void publishStepCompleted(@NotBlank String sagaId,
      @NotBlank String stepName,
      int stepOrder,
      long durationMs,
      @NotNull Map<String, Object> resultData);

  /**
   * Publish step completed event with entity
   */
  void publishStepCompleted(@NotNull SagaStepEntity step);

  /**
   * Publish step failed event
   */
  void publishStepFailed(@NotBlank String sagaId,
      @NotBlank String stepName,
      int stepOrder,
      @NotBlank String errorMessage,
      int retryCount);

  /**
   * Publish step failed event with entity
   */
  void publishStepFailed(@NotNull SagaStepEntity step, @NotBlank String errorMessage);

  /**
   * Publish step skipped event
   */
  void publishStepSkipped(@NotBlank String sagaId,
      @NotBlank String stepName,
      int stepOrder,
      @NotBlank String reason);

  /**
   * Publish step retry event
   */
  void publishStepRetry(@NotBlank String sagaId,
      @NotBlank String stepName,
      int stepOrder,
      int retryCount,
      long delayMs);

  /**
   * Publish step timeout event
   */
  void publishStepTimeout(@NotBlank String sagaId,
      @NotBlank String stepName,
      int stepOrder,
      long timeoutMs);

  /**
   * Publish step status changed event
   */
  void publishStepStatusChanged(@NotBlank String sagaId,
      @NotBlank String stepName,
      @NotNull SagaStepStatus oldStatus,
      @NotNull SagaStepStatus newStatus);

  // ========== Compensation Events ==========

  /**
   * Publish compensation started event
   */
  void publishCompensationStarted(@NotBlank String sagaId, int totalSteps);

  /**
   * Publish compensation completed event
   */
  void publishCompensationCompleted(@NotBlank String sagaId,
      int totalSteps,
      int successfulSteps,
      int failedSteps,
      long durationMs);

  /**
   * Publish compensation failed event
   */
  void publishCompensationFailed(@NotBlank String sagaId,
      @NotBlank String errorMessage,
      int totalSteps,
      int completedSteps);

  /**
   * Publish compensation step started event
   */
  void publishCompensationStepStarted(@NotBlank String sagaId,
      @NotBlank String stepName);

  /**
   * Publish compensation step started event with entity
   */
  void publishCompensationStepStarted(@NotNull SagaCompensationEntity compensation);

  /**
   * Publish compensation step completed event
   */
  void publishCompensationStepCompleted(@NotBlank String sagaId,
      @NotBlank String stepName);

  /**
   * Publish compensation step completed event with entity
   */
  void publishCompensationStepCompleted(@NotNull SagaCompensationEntity compensation);

  /**
   * Publish compensation step failed event
   */
  void publishCompensationStepFailed(@NotBlank String sagaId,
      @NotBlank String stepName,
      @NotBlank String errorMessage);

  /**
   * Publish compensation step failed event with entity
   */
  void publishCompensationStepFailed(@NotNull SagaCompensationEntity compensation,
      @NotBlank String errorMessage);

  /**
   * Publish compensation step retry event
   */
  void publishCompensationStepRetry(@NotBlank String sagaId,
      @NotBlank String stepName,
      int retryCount);

  /**
   * Publish compensation status changed event
   */
  void publishCompensationStatusChanged(@NotBlank String sagaId,
      @NotBlank String stepName,
      @NotNull SagaCompensationStatus oldStatus,
      @NotNull SagaCompensationStatus newStatus);

  // ========== Progress and Monitoring Events ==========

  /**
   * Publish saga progress update event
   */
  void publishSagaProgress(@NotBlank String sagaId,
      int totalSteps,
      int completedSteps,
      int failedSteps,
      double progressPercentage);

  /**
   * Publish saga metrics event
   */
  void publishSagaMetrics(@NotBlank String sagaId,
      @NotNull Map<String, Object> metrics);

  /**
   * Publish saga warning event
   */
  void publishSagaWarning(@NotBlank String sagaId,
      @NotBlank String warningMessage,
      String stepName);

  /**
   * Publish saga performance event
   */
  void publishSagaPerformance(@NotBlank String sagaId,
      long executionTimeMs,
      int stepCount,
      double averageStepDuration);

  // ========== Asynchronous Event Publishing ==========

  /**
   * Publish saga event asynchronously
   */
  CompletableFuture<Void> publishSagaEventAsync(@NotNull SagaEvent event);

  /**
   * Publish step event asynchronously
   */
  CompletableFuture<Void> publishStepEventAsync(@NotNull SagaStepEvent event);

  /**
   * Publish compensation event asynchronously
   */
  CompletableFuture<Void> publishCompensationEventAsync(@NotNull SagaCompensationEvent event);

  // ========== Batch Event Publishing ==========

  /**
   * Publish multiple events in batch
   */
  void publishEventsBatch(@NotNull java.util.List<SagaEvent> events);

  /**
   * Publish multiple events in batch asynchronously
   */
  CompletableFuture<Void> publishEventsBatchAsync(@NotNull java.util.List<SagaEvent> events);

  // ========== Event Base Classes ==========

  /**
   * Base class for all saga events
   */
  abstract class SagaEvent {

    protected final String sagaId;
    protected final String eventType;
    protected final LocalDateTime timestamp;
    protected final Map<String, Object> metadata;

    protected SagaEvent(String sagaId, String eventType, Map<String, Object> metadata) {
      this.sagaId = sagaId;
      this.eventType = eventType;
      this.timestamp = LocalDateTime.now();
      this.metadata = metadata != null ? metadata : Map.of();
    }

    // Getters
    public String getSagaId() {
      return sagaId;
    }

    public String getEventType() {
      return eventType;
    }

    public LocalDateTime getTimestamp() {
      return timestamp;
    }

    public Map<String, Object> getMetadata() {
      return metadata;
    }
  }

  /**
   * Saga step event
   */
  abstract class SagaStepEvent extends SagaEvent {

    protected final String stepName;
    protected final int stepOrder;

    protected SagaStepEvent(String sagaId, String stepName, int stepOrder,
        String eventType, Map<String, Object> metadata) {
      super(sagaId, eventType, metadata);
      this.stepName = stepName;
      this.stepOrder = stepOrder;
    }

    // Getters
    public String getStepName() {
      return stepName;
    }

    public int getStepOrder() {
      return stepOrder;
    }
  }

  /**
   * Saga compensation event
   */
  abstract class SagaCompensationEvent extends SagaEvent {

    protected final String stepName;
    protected final String compensationId;

    protected SagaCompensationEvent(String sagaId, String stepName, String compensationId,
        String eventType, Map<String, Object> metadata) {
      super(sagaId, eventType, metadata);
      this.stepName = stepName;
      this.compensationId = compensationId;
    }

    // Getters
    public String getStepName() {
      return stepName;
    }

    public String getCompensationId() {
      return compensationId;
    }
  }

  // ========== Specific Event Types ==========

  /**
   * Saga started event
   */
  class SagaStartedEvent extends SagaEvent {

    private final String orchestratorName;
    private final Map<String, Object> sagaData;

    public SagaStartedEvent(String sagaId, String orchestratorName,
        Map<String, Object> sagaData, Map<String, Object> metadata) {
      super(sagaId, "SAGA_STARTED", metadata);
      this.orchestratorName = orchestratorName;
      this.sagaData = sagaData;
    }

    public String getOrchestratorName() {
      return orchestratorName;
    }

    public Map<String, Object> getSagaData() {
      return sagaData;
    }
  }

  /**
   * Saga completed event
   */
  class SagaCompletedEvent extends SagaEvent {

    private final String orchestratorName;
    private final long durationMs;

    public SagaCompletedEvent(String sagaId, String orchestratorName,
        long durationMs, Map<String, Object> metadata) {
      super(sagaId, "SAGA_COMPLETED", metadata);
      this.orchestratorName = orchestratorName;
      this.durationMs = durationMs;
    }

    public String getOrchestratorName() {
      return orchestratorName;
    }

    public long getDurationMs() {
      return durationMs;
    }
  }

  /**
   * Saga failed event
   */
  class SagaFailedEvent extends SagaEvent {

    private final String orchestratorName;
    private final String errorMessage;
    private final String failedStepName;

    public SagaFailedEvent(String sagaId, String orchestratorName, String errorMessage,
        String failedStepName, Map<String, Object> metadata) {
      super(sagaId, "SAGA_FAILED", metadata);
      this.orchestratorName = orchestratorName;
      this.errorMessage = errorMessage;
      this.failedStepName = failedStepName;
    }

    public String getOrchestratorName() {
      return orchestratorName;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public String getFailedStepName() {
      return failedStepName;
    }
  }

  /**
   * Step started event
   */
  class StepStartedEvent extends SagaStepEvent {

    private final Map<String, Object> stepData;

    public StepStartedEvent(String sagaId, String stepName, int stepOrder,
        Map<String, Object> stepData, Map<String, Object> metadata) {
      super(sagaId, stepName, stepOrder, "STEP_STARTED", metadata);
      this.stepData = stepData;
    }

    public Map<String, Object> getStepData() {
      return stepData;
    }
  }

  /**
   * Step completed event
   */
  class StepCompletedEvent extends SagaStepEvent {

    private final long durationMs;
    private final Map<String, Object> resultData;

    public StepCompletedEvent(String sagaId, String stepName, int stepOrder,
        long durationMs, Map<String, Object> resultData,
        Map<String, Object> metadata) {
      super(sagaId, stepName, stepOrder, "STEP_COMPLETED", metadata);
      this.durationMs = durationMs;
      this.resultData = resultData;
    }

    public long getDurationMs() {
      return durationMs;
    }

    public Map<String, Object> getResultData() {
      return resultData;
    }
  }

  /**
   * Step failed event
   */
  class StepFailedEvent extends SagaStepEvent {

    private final String errorMessage;
    private final int retryCount;

    public StepFailedEvent(String sagaId, String stepName, int stepOrder,
        String errorMessage, int retryCount, Map<String, Object> metadata) {
      super(sagaId, stepName, stepOrder, "STEP_FAILED", metadata);
      this.errorMessage = errorMessage;
      this.retryCount = retryCount;
    }

    public String getErrorMessage() {
      return errorMessage;
    }

    public int getRetryCount() {
      return retryCount;
    }
  }

  /**
   * Compensation started event
   */
  class CompensationStartedEvent extends SagaEvent {

    private final int totalSteps;

    public CompensationStartedEvent(String sagaId, int totalSteps, Map<String, Object> metadata) {
      super(sagaId, "COMPENSATION_STARTED", metadata);
      this.totalSteps = totalSteps;
    }

    public int getTotalSteps() {
      return totalSteps;
    }
  }

  /**
   * Compensation step started event
   */
  class CompensationStepStartedEvent extends SagaCompensationEvent {

    public CompensationStepStartedEvent(String sagaId, String stepName, String compensationId,
        Map<String, Object> metadata) {
      super(sagaId, stepName, compensationId, "COMPENSATION_STEP_STARTED", metadata);
    }
  }

  /**
   * Compensation step completed event
   */
  class CompensationStepCompletedEvent extends SagaCompensationEvent {

    public CompensationStepCompletedEvent(String sagaId, String stepName, String compensationId,
        Map<String, Object> metadata) {
      super(sagaId, stepName, compensationId, "COMPENSATION_STEP_COMPLETED", metadata);
    }
  }

  /**
   * Compensation step failed event
   */
  class CompensationStepFailedEvent extends SagaCompensationEvent {

    private final String errorMessage;

    public CompensationStepFailedEvent(String sagaId, String stepName, String compensationId,
        String errorMessage, Map<String, Object> metadata) {
      super(sagaId, stepName, compensationId, "COMPENSATION_STEP_FAILED", metadata);
      this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
      return errorMessage;
    }
  }
}
