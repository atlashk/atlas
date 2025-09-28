package org.atlas.framework.saga.exception;

import java.util.Map;

/**
 * Exception thrown when saga compensation operations fail.
 */
public class SagaCompensationException extends SagaException {

    private final String stepName;
    private final String compensationId;
    private final int retryCount;

    public SagaCompensationException(String message, String sagaId, String stepName) {
        super(message, sagaId, "SAGA_COMPENSATION_FAILED");
        this.stepName = stepName;
        this.compensationId = null;
        this.retryCount = 0;
    }

    public SagaCompensationException(String message, Throwable cause, String sagaId, 
                                   String stepName, String compensationId) {
        super(message, cause, sagaId, "SAGA_COMPENSATION_FAILED", true, null);
        this.stepName = stepName;
        this.compensationId = compensationId;
        this.retryCount = 0;
    }

    public SagaCompensationException(String message, Throwable cause, String sagaId, 
                                   String stepName, String compensationId, int retryCount) {
        super(message, cause, sagaId, "SAGA_COMPENSATION_FAILED", true, null);
        this.stepName = stepName;
        this.compensationId = compensationId;
        this.retryCount = retryCount;
    }

    public SagaCompensationException(String message, Throwable cause, String sagaId, 
                                   String stepName, String compensationId, int retryCount,
                                   Map<String, Object> context) {
        super(message, cause, sagaId, "SAGA_COMPENSATION_FAILED", true, context);
        this.stepName = stepName;
        this.compensationId = compensationId;
        this.retryCount = retryCount;
    }

    // Getters
    public String getStepName() {
        return stepName;
    }

    public String getCompensationId() {
        return compensationId;
    }

    public int getRetryCount() {
        return retryCount;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Saga Compensation Exception");
        
        if (getSagaId() != null) {
            sb.append(" [Saga ID: ").append(getSagaId()).append("]");
        }
        
        if (compensationId != null) {
            sb.append(" [Compensation ID: ").append(compensationId).append("]");
        }
        
        if (stepName != null) {
            sb.append(" [Step: ").append(stepName).append("]");
        }
        
        if (retryCount > 0) {
            sb.append(" [Retry Count: ").append(retryCount).append("]");
        }
        
        sb.append(" [Timestamp: ").append(getTimestamp()).append("]");
        sb.append(": ").append(getMessage());
        
        if (!getContext().isEmpty()) {
            sb.append(" [Context: ").append(getContext()).append("]");
        }
        
        return sb.toString();
    }

    // Static factory methods
    public static SagaCompensationException executionFailed(String sagaId, String stepName, 
                                                           Throwable cause) {
        SagaCompensationException exception = new SagaCompensationException(
            "Compensation execution failed: " + cause.getMessage(), 
            cause, sagaId, stepName, null
        );
        exception.addContext("reason", "EXECUTION_FAILED");
        return exception;
    }

    public static SagaCompensationException notFound(String sagaId, String stepName) {
        SagaCompensationException exception = new SagaCompensationException(
            "Compensation not found for step: " + stepName, sagaId, stepName
        );
        exception.addContext("reason", "COMPENSATION_NOT_FOUND");
        return exception;
    }

    public static SagaCompensationException timeout(String sagaId, String stepName, 
                                                   String compensationId, long timeoutMs) {
        SagaCompensationException exception = new SagaCompensationException(
            "Compensation execution timed out after " + timeoutMs + "ms", 
            null, sagaId, stepName, compensationId
        );
        exception.addContext("reason", "TIMEOUT");
        exception.addContext("timeoutMs", timeoutMs);
        return exception;
    }

    public static SagaCompensationException maxRetriesExceeded(String sagaId, String stepName, 
                                                             String compensationId, int maxRetries) {
        SagaCompensationException exception = new SagaCompensationException(
            "Compensation exceeded maximum retries: " + maxRetries, 
            null, sagaId, stepName, compensationId, maxRetries
        );
        exception.addContext("reason", "MAX_RETRIES_EXCEEDED");
        exception.addContext("maxRetries", maxRetries);
        return exception;
    }

    public static SagaCompensationException invalidStatus(String sagaId, String stepName, 
                                                        String compensationId, String currentStatus, 
                                                        String expectedStatus) {
        SagaCompensationException exception = new SagaCompensationException(
            "Invalid compensation status. Expected: " + expectedStatus + ", Current: " + currentStatus, 
            null, sagaId, stepName, compensationId
        );
        exception.addContext("reason", "INVALID_STATUS");
        exception.addContext("currentStatus", currentStatus);
        exception.addContext("expectedStatus", expectedStatus);
        return exception;
    }

    public static SagaCompensationException noCompensationMethod(String sagaId, String stepName) {
        SagaCompensationException exception = new SagaCompensationException(
            "No compensation method found for step: " + stepName, sagaId, stepName
        );
        exception.addContext("reason", "NO_COMPENSATION_METHOD");
        return exception;
    }
}