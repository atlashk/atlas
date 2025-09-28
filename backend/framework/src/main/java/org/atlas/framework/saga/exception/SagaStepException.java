package org.atlas.framework.saga.exception;

import java.util.Map;

/**
 * Exception thrown when saga step operations fail.
 */
public class SagaStepException extends SagaException {

  private final String stepName;
  private final int stepOrder;
  private final String stepId;

  public SagaStepException(String message, String sagaId, String stepName) {
    super(message, sagaId, "SAGA_STEP_FAILED");
    this.stepName = stepName;
    this.stepOrder = -1;
    this.stepId = null;
  }

  public SagaStepException(String message, String sagaId, String stepName, int stepOrder) {
    super(message, sagaId, "SAGA_STEP_FAILED");
    this.stepName = stepName;
    this.stepOrder = stepOrder;
    this.stepId = null;
  }

  public SagaStepException(String message, Throwable cause, String sagaId,
      String stepName, int stepOrder, String stepId) {
    super(message, cause, sagaId, "SAGA_STEP_FAILED", true, null);
    this.stepName = stepName;
    this.stepOrder = stepOrder;
    this.stepId = stepId;
  }

  public SagaStepException(String message, Throwable cause, String sagaId,
      String stepName, int stepOrder, String stepId,
      Map<String, Object> context) {
    super(message, cause, sagaId, "SAGA_STEP_FAILED", true, context);
    this.stepName = stepName;
    this.stepOrder = stepOrder;
    this.stepId = stepId;
  }

  // Getters
  public String getStepName() {
    return stepName;
  }

  public int getStepOrder() {
    return stepOrder;
  }

  public String getStepId() {
    return stepId;
  }

  @Override
  public String getFormattedMessage() {
    StringBuilder sb = new StringBuilder();
    sb.append("Saga Step Exception");

    if (getSagaId() != null) {
      sb.append(" [Saga ID: ").append(getSagaId()).append("]");
    }

    if (stepId != null) {
      sb.append(" [Step ID: ").append(stepId).append("]");
    }

    if (stepName != null) {
      sb.append(" [Step: ").append(stepName);
      if (stepOrder >= 0) {
        sb.append(" (Order: ").append(stepOrder).append(")");
      }
      sb.append("]");
    }

    sb.append(" [Timestamp: ").append(getTimestamp()).append("]");
    sb.append(": ").append(getMessage());

    if (!getContext().isEmpty()) {
      sb.append(" [Context: ").append(getContext()).append("]");
    }

    return sb.toString();
  }

  // Static factory methods
  public static SagaStepException notFound(String sagaId, String stepName) {
    SagaStepException exception = new SagaStepException(
        "Step not found: " + stepName, sagaId, stepName
    );
    exception.addContext("reason", "STEP_NOT_FOUND");
    return exception;
  }

  public static SagaStepException executionFailed(String sagaId, String stepName,
      int stepOrder, Throwable cause) {
    SagaStepException exception = new SagaStepException(
        "Step execution failed: " + cause.getMessage(),
        cause, sagaId, stepName, stepOrder, null
    );
    exception.addContext("reason", "EXECUTION_FAILED");
    return exception;
  }

  public static SagaStepException timeout(String sagaId, String stepName,
      int stepOrder, long timeoutMs) {
    SagaStepException exception = new SagaStepException(
        "Step execution timed out after " + timeoutMs + "ms",
        sagaId, stepName, stepOrder
    );
    exception.addContext("reason", "TIMEOUT");
    exception.addContext("timeoutMs", timeoutMs);
    return exception;
  }

  public static SagaStepException invalidStatus(String sagaId, String stepName,
      String currentStatus, String expectedStatus) {
    SagaStepException exception = new SagaStepException(
        "Invalid step status. Expected: " + expectedStatus + ", Current: " + currentStatus,
        sagaId, stepName
    );
    exception.addContext("reason", "INVALID_STATUS");
    exception.addContext("currentStatus", currentStatus);
    exception.addContext("expectedStatus", expectedStatus);
    return exception;
  }

  public static SagaStepException maxRetriesExceeded(String sagaId, String stepName,
      int stepOrder, int maxRetries) {
    SagaStepException exception = new SagaStepException(
        "Step exceeded maximum retries: " + maxRetries,
        sagaId, stepName, stepOrder
    );
    exception.addContext("reason", "MAX_RETRIES_EXCEEDED");
    exception.addContext("maxRetries", maxRetries);
    return exception;
  }
}