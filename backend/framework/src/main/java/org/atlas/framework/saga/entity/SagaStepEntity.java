package org.atlas.framework.saga.entity;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import javax.validation.constraints.Min;
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
 * Entity representing a single step within a saga orchestration.
 * Contains the state and metadata for an individual transaction step.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"errorMessage"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaStepEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long stepId;

  @NotNull(message = "Saga ID cannot be null")
  private Long sagaId;

  @NotBlank(message = "Step name cannot be blank")
  @Size(max = 255, message = "Step name cannot exceed 255 characters")
  private String stepName;

  @NotNull(message = "Step order cannot be null")
  @Min(value = 0, message = "Step order must be non-negative")
  private Integer stepOrder;

  @NotNull(message = "Step status cannot be null")
  private SagaStepStatus stepStatus;

  @Size(max = 255, message = "Compensation method cannot exceed 255 characters")
  private String compensationMethod;

  private LocalDateTime startedAt;

  private LocalDateTime completedAt;

  @Size(max = 2000, message = "Error message cannot exceed 2000 characters")
  private String errorMessage;

  @Builder.Default
  @Min(value = 0, message = "Retry count must be non-negative")
  private Integer retryCount = 0;

  @Builder.Default
  @Min(value = 0, message = "Max retries must be non-negative")
  private Integer maxRetries = 0;

  @Builder.Default
  private Long timeoutMs = 30000L; // 30 seconds default

  // Business logic methods

  /**
   * Check if the step is in a terminal state
   */
  public boolean isTerminal() {
    return stepStatus != null && (
        stepStatus == SagaStepStatus.COMPLETED ||
        stepStatus == SagaStepStatus.FAILED ||
        stepStatus == SagaStepStatus.SKIPPED ||
        stepStatus == SagaStepStatus.COMPENSATED ||
        stepStatus == SagaStepStatus.COMPENSATION_FAILED
    );
  }

  /**
   * Check if the step is currently running
   */
  public boolean isRunning() {
    return stepStatus != null && (
        stepStatus == SagaStepStatus.IN_PROGRESS ||
        stepStatus == SagaStepStatus.RETRYING ||
        stepStatus == SagaStepStatus.COMPENSATING
    );
  }

  /**
   * Check if the step completed successfully
   */
  public boolean isSuccessful() {
    return stepStatus == SagaStepStatus.COMPLETED;
  }

  /**
   * Check if the step failed
   */
  public boolean isFailed() {
    return stepStatus == SagaStepStatus.FAILED || stepStatus == SagaStepStatus.COMPENSATION_FAILED;
  }

  /**
   * Check if the step can be retried
   */
  public boolean canRetry() {
    return stepStatus == SagaStepStatus.FAILED && 
           retryCount != null && maxRetries != null && 
           retryCount < maxRetries;
  }

  /**
   * Check if the step has compensation method
   */
  public boolean hasCompensation() {
    return compensationMethod != null && !compensationMethod.trim().isEmpty();
  }

  /**
   * Get the duration of the step execution in milliseconds
   */
  public Long getDurationMs() {
    if (startedAt == null) {
      return null;
    }
    
    LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
    return ChronoUnit.MILLIS.between(startedAt, endTime);
  }

  /**
   * Start step execution
   */
  public void start() {
    this.stepStatus = SagaStepStatus.IN_PROGRESS;
    this.startedAt = LocalDateTime.now();
    this.errorMessage = null;
  }

  /**
   * Mark step as completed
   */
  public void markCompleted() {
    this.stepStatus = SagaStepStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = null;
  }

  /**
   * Mark step as failed with error message
   */
  public void markFailed(String errorMessage) {
    this.stepStatus = SagaStepStatus.FAILED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }

  /**
   * Mark step as skipped
   */
  public void markSkipped() {
    this.stepStatus = SagaStepStatus.SKIPPED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = null;
  }

  /**
   * Start retry attempt
   */
  public void startRetry() {
    if (!canRetry()) {
      throw new IllegalStateException("Cannot retry step: " + stepName);
    }
    this.stepStatus = SagaStepStatus.RETRYING;
    this.retryCount++;
    this.startedAt = LocalDateTime.now();
    this.completedAt = null;
    this.errorMessage = null;
  }

  /**
   * Start compensation
   */
  public void startCompensation() {
    if (!hasCompensation()) {
      throw new IllegalStateException("Step has no compensation method: " + stepName);
    }
    this.stepStatus = SagaStepStatus.COMPENSATING;
  }

  /**
   * Mark compensation as completed
   */
  public void markCompensated() {
    this.stepStatus = SagaStepStatus.COMPENSATED;
    this.completedAt = LocalDateTime.now();
  }

  /**
   * Mark compensation as failed
   */
  public void markCompensationFailed(String errorMessage) {
    this.stepStatus = SagaStepStatus.COMPENSATION_FAILED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }

  /**
   * Check if step execution has timed out
   */
  public boolean isTimedOut() {
    if (startedAt == null || timeoutMs == null || timeoutMs <= 0) {
      return false;
    }
    
    return isRunning() && getDurationMs() > timeoutMs;
  }

  /**
   * Validate entity state
   */
  public void validate() {
    Objects.requireNonNull(sagaId, "Saga ID cannot be null");
    Objects.requireNonNull(stepName, "Step name cannot be null");
    Objects.requireNonNull(stepOrder, "Step order cannot be null");
    Objects.requireNonNull(stepStatus, "Step status cannot be null");
    Objects.requireNonNull(retryCount, "Retry count cannot be null");
    Objects.requireNonNull(maxRetries, "Max retries cannot be null");
    
    if (stepOrder < 0) {
      throw new IllegalArgumentException("Step order must be non-negative");
    }
    
    if (retryCount < 0) {
      throw new IllegalArgumentException("Retry count must be non-negative");
    }
    
    if (maxRetries < 0) {
      throw new IllegalArgumentException("Max retries must be non-negative");
    }
    
    if (retryCount > maxRetries) {
      throw new IllegalArgumentException("Retry count cannot exceed max retries");
    }
    
    if (isTerminal() && startedAt != null && completedAt == null) {
      throw new IllegalStateException("Terminal step must have completion timestamp");
    }
    
    if (completedAt != null && startedAt != null && startedAt.isAfter(completedAt)) {
      throw new IllegalStateException("Started at timestamp cannot be after completed at timestamp");
    }
  }
}
