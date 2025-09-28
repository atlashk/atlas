package org.atlas.framework.saga.entity;

import java.util.Set;

/**
 * Enumeration representing the various states of a saga step.
 * Provides utility methods for state validation and transitions.
 */
public enum SagaStepStatus {

  PENDING("Step is pending execution"),
  IN_PROGRESS("Step is currently executing"),
  COMPLETED("Step completed successfully"),
  FAILED("Step execution failed"),
  SKIPPED("Step was skipped"),
  RETRYING("Step is being retried"),
  COMPENSATING("Step is performing compensation"),
  COMPENSATED("Step compensation completed"),
  COMPENSATION_FAILED("Step compensation failed");

  private final String description;

  SagaStepStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Terminal states that indicate step execution is finished
   */
  private static final Set<SagaStepStatus> TERMINAL_STATES = Set.of(
      COMPLETED, FAILED, SKIPPED, COMPENSATED, COMPENSATION_FAILED
  );

  /**
   * Active states that indicate step is currently processing
   */
  private static final Set<SagaStepStatus> ACTIVE_STATES = Set.of(
      IN_PROGRESS, RETRYING, COMPENSATING
  );

  /**
   * Success states that indicate positive outcomes
   */
  private static final Set<SagaStepStatus> SUCCESS_STATES = Set.of(
      COMPLETED, SKIPPED, COMPENSATED
  );

  /**
   * Failure states that indicate negative outcomes
   */
  private static final Set<SagaStepStatus> FAILURE_STATES = Set.of(
      FAILED, COMPENSATION_FAILED
  );

  /**
   * States that allow retry
   */
  private static final Set<SagaStepStatus> RETRYABLE_STATES = Set.of(
      FAILED
  );

  /**
   * Check if this status is terminal (step execution finished)
   */
  public boolean isTerminal() {
    return TERMINAL_STATES.contains(this);
  }

  /**
   * Check if this status is active (step is currently processing)
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
   * Check if step can be retried from this status
   */
  public boolean canRetry() {
    return RETRYABLE_STATES.contains(this);
  }

  /**
   * Check if compensation can be started from this status
   */
  public boolean canStartCompensation() {
    return this == COMPLETED || this == FAILED;
  }

  /**
   * Check if this status allows step execution
   */
  public boolean allowsExecution() {
    return this == PENDING || this == RETRYING;
  }

  /**
   * Get the next status when starting execution
   */
  public SagaStepStatus getNextStatusOnStart() {
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
  public SagaStepStatus getNextStatusOnSuccess() {
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
  public SagaStepStatus getNextStatusOnFailure() {
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
  public SagaStepStatus getNextStatusOnRetry() {
    switch (this) {
      case FAILED:
        return RETRYING;
      default:
        throw new IllegalStateException("Cannot retry from " + this);
    }
  }

  /**
   * Get the next status when skipping step
   */
  public SagaStepStatus getNextStatusOnSkip() {
    switch (this) {
      case PENDING:
        return SKIPPED;
      default:
        throw new IllegalStateException("Cannot skip from " + this);
    }
  }

  /**
   * Get the next status when starting compensation
   */
  public SagaStepStatus getNextStatusOnCompensationStart() {
    switch (this) {
      case COMPLETED:
      case FAILED:
        return COMPENSATING;
      default:
        throw new IllegalStateException("Cannot start compensation from " + this);
    }
  }

  /**
   * Get the next status after successful compensation
   */
  public SagaStepStatus getNextStatusOnCompensationSuccess() {
    switch (this) {
      case COMPENSATING:
        return COMPENSATED;
      default:
        throw new IllegalStateException("Cannot complete compensation from " + this);
    }
  }

  /**
   * Get the next status after compensation failure
   */
  public SagaStepStatus getNextStatusOnCompensationFailure() {
    switch (this) {
      case COMPENSATING:
        return COMPENSATION_FAILED;
      default:
        throw new IllegalStateException("Cannot fail compensation from " + this);
    }
  }

  /**
   * Validate if transition to target status is allowed
   */
  public boolean canTransitionTo(SagaStepStatus targetStatus) {
    if (this == targetStatus) {
      return true;
    }

    switch (this) {
      case PENDING:
        return targetStatus == IN_PROGRESS || targetStatus == SKIPPED;
      case IN_PROGRESS:
        return targetStatus == COMPLETED || targetStatus == FAILED;
      case FAILED:
        return targetStatus == RETRYING || targetStatus == COMPENSATING;
      case RETRYING:
        return targetStatus == IN_PROGRESS;
      case COMPLETED:
        return targetStatus == COMPENSATING;
      case COMPENSATING:
        return targetStatus == COMPENSATED || targetStatus == COMPENSATION_FAILED;
      case SKIPPED:
      case COMPENSATED:
      case COMPENSATION_FAILED:
        return false; // Terminal states cannot transition
      default:
        return false;
    }
  }

  /**
   * Get all valid next statuses from current status
   */
  public Set<SagaStepStatus> getValidNextStatuses() {
    switch (this) {
      case PENDING:
        return Set.of(IN_PROGRESS, SKIPPED);
      case IN_PROGRESS:
        return Set.of(COMPLETED, FAILED);
      case FAILED:
        return Set.of(RETRYING, COMPENSATING);
      case RETRYING:
        return Set.of(IN_PROGRESS);
      case COMPLETED:
        return Set.of(COMPENSATING);
      case COMPENSATING:
        return Set.of(COMPENSATED, COMPENSATION_FAILED);
      case SKIPPED:
      case COMPENSATED:
      case COMPENSATION_FAILED:
        return Set.of(); // Terminal states
      default:
        return Set.of();
    }
  }
}
