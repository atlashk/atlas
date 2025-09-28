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
 * Entity representing a compensation action for a saga step.
 * Contains the state and metadata for undoing a completed transaction step.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"errorMessage"})
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class SagaCompensationEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Long compensationId;

  @NotNull(message = "Saga ID cannot be null")
  private Long sagaId;

  @NotNull(message = "Step ID cannot be null")
  private Long stepId;

  @NotBlank(message = "Compensation name cannot be blank")
  @Size(max = 255, message = "Compensation name cannot exceed 255 characters")
  private String compensationName;

  @NotBlank(message = "Compensation method cannot be blank")
  @Size(max = 255, message = "Compensation method cannot exceed 255 characters")
  private String compensationMethod;

  @NotNull(message = "Compensation status cannot be null")
  private SagaCompensationStatus compensationStatus;

  private LocalDateTime startedAt;

  private LocalDateTime completedAt;

  @Size(max = 2000, message = "Error message cannot exceed 2000 characters")
  private String errorMessage;

  @Builder.Default
  @Min(value = 0, message = "Retry count must be non-negative")
  private Integer retryCount = 0;

  @Builder.Default
  @Min(value = 0, message = "Max retries must be non-negative")
  private Integer maxRetries = 3;

  @Builder.Default
  private Long timeoutMs = 30000L; // 30 seconds default

  @Builder.Default
  private Long retryDelayMs = 5000L; // 5 seconds default

  // Business logic methods

  /**
   * Check if the compensation is in a terminal state
   */
  public boolean isTerminal() {
    return compensationStatus != null && (
        compensationStatus == SagaCompensationStatus.COMPLETED ||
        compensationStatus == SagaCompensationStatus.FAILED ||
        compensationStatus == SagaCompensationStatus.MAX_RETRIES_EXCEEDED
    );
  }

  /**
   * Check if the compensation is currently running
   */
  public boolean isRunning() {
    return compensationStatus != null && (
        compensationStatus == SagaCompensationStatus.IN_PROGRESS ||
        compensationStatus == SagaCompensationStatus.RETRYING
    );
  }

  /**
   * Check if the compensation completed successfully
   */
  public boolean isSuccessful() {
    return compensationStatus == SagaCompensationStatus.COMPLETED;
  }

  /**
   * Check if the compensation failed
   */
  public boolean isFailed() {
    return compensationStatus == SagaCompensationStatus.FAILED ||
           compensationStatus == SagaCompensationStatus.MAX_RETRIES_EXCEEDED;
  }

  /**
   * Check if the compensation can be retried
   */
  public boolean canRetry() {
    return compensationStatus == SagaCompensationStatus.FAILED && 
           retryCount != null && maxRetries != null && 
           retryCount < maxRetries;
  }

  /**
   * Check if max retries have been exceeded
   */
  public boolean hasExceededMaxRetries() {
    return retryCount != null && maxRetries != null && retryCount >= maxRetries;
  }

  /**
   * Get the duration of the compensation execution in milliseconds
   */
  public Long getDurationMs() {
    if (startedAt == null) {
      return null;
    }
    
    LocalDateTime endTime = completedAt != null ? completedAt : LocalDateTime.now();
    return ChronoUnit.MILLIS.between(startedAt, endTime);
  }

  /**
   * Start compensation execution
   */
  public void start() {
    this.compensationStatus = SagaCompensationStatus.IN_PROGRESS;
    this.startedAt = LocalDateTime.now();
    this.errorMessage = null;
  }

  /**
   * Mark compensation as completed
   */
  public void markCompleted() {
    this.compensationStatus = SagaCompensationStatus.COMPLETED;
    this.completedAt = LocalDateTime.now();
    this.errorMessage = null;
  }

  /**
   * Mark compensation as failed with error message
   */
  public void markFailed(String errorMessage) {
    if (hasExceededMaxRetries()) {
      this.compensationStatus = SagaCompensationStatus.MAX_RETRIES_EXCEEDED;
    } else {
      this.compensationStatus = SagaCompensationStatus.FAILED;
    }
    this.completedAt = LocalDateTime.now();
    this.errorMessage = errorMessage;
  }

  /**
   * Start retry attempt
   */
  public void startRetry() {
    if (!canRetry()) {
      throw new IllegalStateException("Cannot retry compensation: " + compensationName);
    }
    this.compensationStatus = SagaCompensationStatus.RETRYING;
    this.retryCount++;
    this.startedAt = LocalDateTime.now();
    this.completedAt = null;
    this.errorMessage = null;
  }

  /**
   * Mark as max retries exceeded
   */
  public void markMaxRetriesExceeded() {
    this.compensationStatus = SagaCompensationStatus.MAX_RETRIES_EXCEEDED;
    this.completedAt = LocalDateTime.now();
  }

  /**
   * Check if compensation execution has timed out
   */
  public boolean isTimedOut() {
    if (startedAt == null || timeoutMs == null || timeoutMs <= 0) {
      return false;
    }
    
    return isRunning() && getDurationMs() > timeoutMs;
  }

  /**
   * Get the next retry time based on retry delay
   */
  public LocalDateTime getNextRetryTime() {
    if (completedAt == null || retryDelayMs == null) {
      return LocalDateTime.now();
    }
    
    return completedAt.plus(retryDelayMs, ChronoUnit.MILLIS);
  }

  /**
   * Check if it's time for the next retry
   */
  public boolean isRetryTimeReached() {
    return LocalDateTime.now().isAfter(getNextRetryTime());
  }

  /**
   * Get remaining retry attempts
   */
  public Integer getRemainingRetries() {
    if (retryCount == null || maxRetries == null) {
      return 0;
    }
    return Math.max(0, maxRetries - retryCount);
  }

  /**
   * Validate entity state
   */
  public void validate() {
    Objects.requireNonNull(sagaId, "Saga ID cannot be null");
    Objects.requireNonNull(stepId, "Step ID cannot be null");
    Objects.requireNonNull(compensationName, "Compensation name cannot be null");
    Objects.requireNonNull(compensationMethod, "Compensation method cannot be null");
    Objects.requireNonNull(compensationStatus, "Compensation status cannot be null");
    Objects.requireNonNull(retryCount, "Retry count cannot be null");
    Objects.requireNonNull(maxRetries, "Max retries cannot be null");
    
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
      throw new IllegalStateException("Terminal compensation must have completion timestamp");
    }
    
    if (completedAt != null && startedAt != null && startedAt.isAfter(completedAt)) {
      throw new IllegalStateException("Started at timestamp cannot be after completed at timestamp");
    }
  }
}
