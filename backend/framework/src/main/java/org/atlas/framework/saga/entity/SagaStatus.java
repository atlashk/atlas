package org.atlas.framework.saga.entity;

import java.util.Set;

/**
 * Enumeration representing the various states of a saga orchestration.
 * Provides utility methods for state validation and transitions.
 */
public enum SagaStatus {

  PENDING("Saga is pending execution"),
  STARTED("Saga has been started"),
  IN_PROGRESS("Saga is currently executing steps"),
  COMPLETED("Saga completed successfully"),
  FAILED("Saga execution failed"),
  COMPENSATING("Saga is performing compensation"),
  COMPENSATED("Saga compensation completed"),
  COMPENSATION_FAILED("Saga compensation failed");

  private final String description;

  SagaStatus(String description) {
    this.description = description;
  }

  public String getDescription() {
    return description;
  }

  /**
   * Terminal states that indicate saga execution is finished
   */
  private static final Set<SagaStatus> TERMINAL_STATES = Set.of(
      COMPLETED, FAILED, COMPENSATED, COMPENSATION_FAILED
  );

  /**
   * Active states that indicate saga is currently processing
   */
  private static final Set<SagaStatus> ACTIVE_STATES = Set.of(
      STARTED, IN_PROGRESS, COMPENSATING
  );

  /**
   * Success states that indicate positive outcomes
   */
  private static final Set<SagaStatus> SUCCESS_STATES = Set.of(
      COMPLETED, COMPENSATED
  );

  /**
   * Failure states that indicate negative outcomes
   */
  private static final Set<SagaStatus> FAILURE_STATES = Set.of(
      FAILED, COMPENSATION_FAILED
  );

  /**
   * Check if this status is terminal (saga execution finished)
   */
  public boolean isTerminal() {
    return TERMINAL_STATES.contains(this);
  }

  /**
   * Check if this status is active (saga is currently processing)
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
   * Check if compensation can be started from this status
   */
  public boolean canStartCompensation() {
    return this == FAILED || this == IN_PROGRESS;
  }

  /**
   * Check if this status allows step execution
   */
  public boolean allowsStepExecution() {
    return this == STARTED || this == IN_PROGRESS;
  }

  /**
   * Get the next status after successful step completion
   */
  public SagaStatus getNextStatusOnStepSuccess(boolean hasMoreSteps) {
    switch (this) {
      case STARTED:
      case IN_PROGRESS:
        return hasMoreSteps ? IN_PROGRESS : COMPLETED;
      default:
        throw new IllegalStateException("Cannot transition from " + this + " on step success");
    }
  }

  /**
   * Get the next status after step failure
   */
  public SagaStatus getNextStatusOnStepFailure() {
    switch (this) {
      case STARTED:
      case IN_PROGRESS:
        return FAILED;
      default:
        throw new IllegalStateException("Cannot transition from " + this + " on step failure");
    }
  }

  /**
   * Get the next status when starting compensation
   */
  public SagaStatus getNextStatusOnCompensationStart() {
    switch (this) {
      case FAILED:
      case IN_PROGRESS:
        return COMPENSATING;
      default:
        throw new IllegalStateException("Cannot start compensation from " + this);
    }
  }

  /**
   * Get the next status after successful compensation
   */
  public SagaStatus getNextStatusOnCompensationSuccess() {
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
  public SagaStatus getNextStatusOnCompensationFailure() {
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
  public boolean canTransitionTo(SagaStatus targetStatus) {
    if (this == targetStatus) {
      return true;
    }

    switch (this) {
      case PENDING:
        return targetStatus == STARTED;
      case STARTED:
        return targetStatus == IN_PROGRESS || targetStatus == COMPLETED || targetStatus == FAILED;
      case IN_PROGRESS:
        return targetStatus == COMPLETED || targetStatus == FAILED || targetStatus == COMPENSATING;
      case FAILED:
        return targetStatus == COMPENSATING;
      case COMPENSATING:
        return targetStatus == COMPENSATED || targetStatus == COMPENSATION_FAILED;
      case COMPLETED:
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
  public Set<SagaStatus> getValidNextStatuses() {
    switch (this) {
      case PENDING:
        return Set.of(STARTED);
      case STARTED:
        return Set.of(IN_PROGRESS, COMPLETED, FAILED);
      case IN_PROGRESS:
        return Set.of(COMPLETED, FAILED, COMPENSATING);
      case FAILED:
        return Set.of(COMPENSATING);
      case COMPENSATING:
        return Set.of(COMPENSATED, COMPENSATION_FAILED);
      case COMPLETED:
      case COMPENSATED:
      case COMPENSATION_FAILED:
        return Set.of(); // Terminal states
      default:
        return Set.of();
    }
  }
}
