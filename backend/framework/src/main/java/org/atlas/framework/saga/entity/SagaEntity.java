package org.atlas.framework.saga.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.atlas.framework.domain.entity.DomainEntity;

/**
 * Entity representing a saga orchestration instance.
 * Contains the overall state and metadata for a distributed transaction.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"errorMessage"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long sagaId;

  private String sagaName;

  private String orchestratorName;

  private SagaStatus sagaStatus;

  private LocalDateTime startedAt;

  private LocalDateTime completedAt;

  private String errorMessage;

  // Business logic methods

  /**
   * Check if the saga is in a terminal state (completed, failed, or compensated)
   */
  public boolean isTerminal() {
    return sagaStatus != null && (
        sagaStatus == SagaStatus.COMPLETED ||
        sagaStatus == SagaStatus.FAILED ||
        sagaStatus == SagaStatus.COMPENSATED ||
        sagaStatus == SagaStatus.COMPENSATION_FAILED
    );
  }

  /**
   * Check if the saga is currently running
   */
  public boolean isRunning() {
    return sagaStatus != null && (
        sagaStatus == SagaStatus.STARTED ||
        sagaStatus == SagaStatus.IN_PROGRESS ||
        sagaStatus == SagaStatus.COMPENSATING
    );
  }

  /**
   * Check if the saga completed successfully
   */
  public boolean isSuccessful() {
    return sagaStatus == SagaStatus.COMPLETED;
  }

  /**
   * Check if the saga failed
   */
  public boolean isFailed() {
    return sagaStatus == SagaStatus.FAILED || sagaStatus == SagaStatus.COMPENSATION_FAILED;
  }

  /**
   * Get the duration of the saga execution in milliseconds
   */
  public Long getDurationMs() {
    if (startedAt == null) {
      return null;
    }
    
    LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
    return ChronoUnit.MILLIS.between(startedAt, endTime);
  }

  /**
   * Mark saga as completed
   */
  public void markCompleted() {
    this.sagaStatus = SagaStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = null;
  }

  /**
   * Mark saga as failed with error message
   */
  public void markFailed(String errorMessage) {
    this.sagaStatus = SagaStatus.FAILED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }

  /**
   * Start compensation process
   */
  public void startCompensation() {
    if (!canStartCompensation()) {
      throw new IllegalStateException("Cannot start compensation for saga in status: " + sagaStatus);
    }
    this.sagaStatus = SagaStatus.COMPENSATING;
  }

  /**
   * Mark compensation as completed
   */
  public void markCompensated() {
    this.sagaStatus = SagaStatus.COMPENSATED;
    this.completedAt = LocalDateTime.now();
  }

  /**
   * Mark compensation as failed
   */
  public void markCompensationFailed(String errorMessage) {
    this.sagaStatus = SagaStatus.COMPENSATION_FAILED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }

  /**
   * Check if compensation can be started
   */
  public boolean canStartCompensation() {
    return sagaStatus == SagaStatus.FAILED || sagaStatus == SagaStatus.IN_PROGRESS;
  }

  /**
   * Validate entity state
   */
  public void validate() {
    Objects.requireNonNull(sagaName, "Saga name cannot be null");
    Objects.requireNonNull(orchestratorName, "Orchestrator name cannot be null");
    Objects.requireNonNull(sagaStatus, "Saga status cannot be null");
    Objects.requireNonNull(startedAt, "Started at timestamp cannot be null");
    
    if (isTerminal() && completedAt == null) {
      throw new IllegalStateException("Terminal saga must have completion timestamp");
    }
    
    if (completedAt != null && startedAt.isAfter(completedAt)) {
      throw new IllegalStateException("Started at timestamp cannot be after completed at timestamp");
    }
  }
}
