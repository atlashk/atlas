package org.atlas.framework.saga.entity;

import java.util.Set;

/**
 * Enumeration representing the various states of a saga compensation.
 * Provides utility methods for state validation and transitions.
 */
public enum SagaCompensationStatus {

  PENDING("Compensation is pending execution"),
  IN_PROGRESS("Compensation is currently executing"),
  COMPLETED("Compensation completed successfully"),
  FAILED("Compensation execution failed"),
  RETRYING("Compensation is being retried"),
  MAX_RETRIES_EXCEEDED("Compensation exceeded maximum retry attempts");

  private final String description;

  SagaCompensationStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Terminal states that indicate compensation execution is finished
   */
  private static final Set<SagaCompensationStatus> TERMINAL_STATES = Set.of(
      COMPLETED, FAILED, MAX_RETRIES_EXCEEDED
  );

  /**
   * Active states that indicate compensation is currently processing
   */
  private static final Set<SagaCompensationStatus> ACTIVE_STATES = Set.of(
      IN_PROGRESS, RETRYING
  );

  /**
   * Success states that indicate positive outcomes
   */
  private static final Set<SagaCompensationStatus> SUCCESS_STATES = Set.of(
      COMPLETED
  );

  /**
   * Failure states that indicate negative outcomes
   */
  private static final Set<SagaCompensationStatus> FAILURE_STATES = Set.of(
      FAILED, MAX_RETRIES_EXCEEDED
  );

  /**
   * States that allow retry
   */
  private static final Set<SagaCompensationStatus> RETRYABLE_STATES = Set.of(
      FAILED
  );

  /**
   * Check if this status is terminal (compensation execution finished)
   */
  public boolean isTerminal() {
    return TERMINAL_STATES.contains(this);
  }

  /**
   * Check if this status is active (compensation is currently processing)
   */
  public boolean isActive() {
    return ACTIVE_STATES.contains(this);
  }

  /**
   * Check if this status represents a successful outcome
   */
  public boolean isSuccess() {
    return SUCCESS_STATES.contains(this);
  }

  /**
   * Check if this status represents a failure
   */
  public boolean isFailure() {
    return FAILURE_STATES.contains(this);
  }

  /**
   * Check if compensation can be retried from this status
   */
  public boolean canRetry() {
    return RETRYABLE_STATES.contains(this);
  }

  /**
   * Check if this status allows compensation execution
   */
  public boolean allowsExecution() {
    return this == PENDING || this == RETRYING;
  }

  /**
   * Check if this status indicates max retries have been exceeded
   */
  public boolean hasExceededMaxRetries() {
    return this == MAX_RETRIES_EXCEEDED;
  }

  /**
   * Get the next status when starting execution
   */
  public SagaCompensationStatus getNextStatusOnStart() {
    switch (this) {
      case PENDING:
      case RETRYING:
        return IN_PROGRESS;
      default:
        throw new IllegalStateException("Cannot start execution from " + this);
    }
  }

  /**
   * Get the next status after successful execution
   */
  public SagaCompensationStatus getNextStatusOnSuccess() {
    switch (this) {
      case IN_PROGRESS:
        return COMPLETED;
      default:
        throw new IllegalStateException("Cannot complete from " + this);
    }
  }

  /**
   * Get the next status after execution failure
   */
  public SagaCompensationStatus getNextStatusOnFailure() {
    switch (this) {
      case IN_PROGRESS:
        return FAILED;
      default:
        throw new IllegalStateException("Cannot fail from " + this);
    }
  }

  /**
   * Get the next status when starting retry
   */
  public SagaCompensationStatus getNextStatusOnRetry() {
    switch (this) {
      case FAILED:
        return RETRYING;
      default:
        throw new IllegalStateException("Cannot retry from " + this);
    }
  }

  /**
   * Get the next status when max retries are exceeded
   */
  public SagaCompensationStatus getNextStatusOnMaxRetriesExceeded() {
    switch (this) {
      case FAILED:
      case RETRYING:
        return MAX_RETRIES_EXCEEDED;
      default:
        throw new IllegalStateException("Cannot exceed max retries from " + this);
    }
  }

  /**
   * Validate if transition to target status is allowed
   */
  public boolean canTransitionTo(SagaCompensationStatus targetStatus) {
    if (this == targetStatus) {
      return true;
    }

    switch (this) {
      case PENDING:
        return targetStatus == IN_PROGRESS;
      case IN_PROGRESS:
        return targetStatus == COMPLETED || targetStatus == FAILED;
      case FAILED:
        return targetStatus == RETRYING || targetStatus == MAX_RETRIES_EXCEEDED;
      case RETRYING:
        return targetStatus == IN_PROGRESS || targetStatus == MAX_RETRIES_EXCEEDED;
      case COMPLETED:
      case MAX_RETRIES_EXCEEDED:
        return false; // Terminal states cannot transition
      default:
        return false;
    }
  }

  /**
   * Get all valid next statuses from current status
   */
  public Set<SagaCompensationStatus> getValidNextStatuses() {
    switch (this) {
      case PENDING:
        return Set.of(IN_PROGRESS);
      case IN_PROGRESS:
        return Set.of(COMPLETED, FAILED);
      case FAILED:
        return Set.of(RETRYING, MAX_RETRIES_EXCEEDED);
      case RETRYING:
        return Set.of(IN_PROGRESS, MAX_RETRIES_EXCEEDED);
      case COMPLETED:
      case MAX_RETRIES_EXCEEDED:
        return Set.of(); // Terminal states
      default:
        return Set.of();
    }
  }

  /**
   * Get priority for compensation execution (lower number = higher priority)
   */
  public int getExecutionPriority() {
    switch (this) {
      case RETRYING:
        return 1; // Highest priority for retries
      case PENDING:
        return 2; // Normal priority for new compensations
      case IN_PROGRESS:
        return 3; // Lower priority for already running
      default:
        return Integer.MAX_VALUE; // No priority for terminal states
    }
  }
}
