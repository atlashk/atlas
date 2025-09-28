package org.atlas.framework.saga.exception;

import java.util.Map;

/**
 * Exception thrown when saga execution fails.
 * Provides specific information about execution failures.
 */
public class SagaExecutionException extends SagaException {

    private final String stepName;
    private final int stepOrder;
    private final String orchestratorName;

    public SagaExecutionException(String message, String sagaId) {
        super(message, sagaId, "SAGA_EXECUTION_FAILED");
    }

    public SagaExecutionException(String message, String sagaId, String stepName) {
        super(message, sagaId, "SAGA_EXECUTION_FAILED");
        this.stepName = stepName;
        this.stepOrder = -1;
        this.orchestratorName = null;
    }

    public SagaExecutionException(String message, Throwable cause, String sagaId, 
                                 String stepName, int stepOrder) {
        super(message, cause, sagaId, "SAGA_EXECUTION_FAILED", true, null);
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.orchestratorName = null;
    }

    public SagaExecutionException(String message, Throwable cause, String sagaId, 
                                 String stepName, int stepOrder, String orchestratorName) {
        super(message, cause, sagaId, "SAGA_EXECUTION_FAILED", true, null);
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.orchestratorName = orchestratorName;
    }

    public SagaExecutionException(String message, Throwable cause, String sagaId, 
                                 String stepName, int stepOrder, String orchestratorName,
                                 Map<String, Object> context) {
        super(message, cause, sagaId, "SAGA_EXECUTION_FAILED", true, context);
        this.stepName = stepName;
        this.stepOrder = stepOrder;
        this.orchestratorName = orchestratorName;
    }

    // Getters
    public String getStepName() {
        return stepName;
    }

    public int getStepOrder() {
        return stepOrder;
    }

    public String getOrchestratorName() {
        return orchestratorName;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Saga Execution Exception");
        
        if (getSagaId() != null) {
            sb.append(" [Saga ID: ").append(getSagaId()).append("]");
        }
        
        if (orchestratorName != null) {
            sb.append(" [Orchestrator: ").append(orchestratorName).append("]");
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
    public static SagaExecutionException stepFailed(String sagaId, String stepName, 
                                                   int stepOrder, String errorMessage) {
        return new SagaExecutionException(
            "Step execution failed: " + errorMessage, 
            null, sagaId, stepName, stepOrder
        );
    }

    public static SagaExecutionException stepFailed(String sagaId, String stepName, 
                                                   int stepOrder, Throwable cause) {
        return new SagaExecutionException(
            "Step execution failed: " + cause.getMessage(), 
            cause, sagaId, stepName, stepOrder
        );
    }

    public static SagaExecutionException stepTimeout(String sagaId, String stepName, 
                                                    int stepOrder, long timeoutMs) {
        SagaExecutionException exception = new SagaExecutionException(
            "Step execution timed out after " + timeoutMs + "ms", 
            null, sagaId, stepName, stepOrder
        );
        exception.addContext("timeoutMs", timeoutMs);
        exception.addContext("reason", "TIMEOUT");
        return exception;
    }

    public static SagaExecutionException orchestratorNotFound(String sagaId, String orchestratorName) {
        SagaExecutionException exception = new SagaExecutionException(
            "Orchestrator not found: " + orchestratorName, 
            sagaId
        );
        exception.addContext("orchestratorName", orchestratorName);
        exception.addContext("reason", "ORCHESTRATOR_NOT_FOUND");
        return exception;
    }

    public static SagaExecutionException invalidSagaState(String sagaId, String currentState, 
                                                         String expectedState) {
        SagaExecutionException exception = new SagaExecutionException(
            "Invalid saga state. Expected: " + expectedState + ", Current: " + currentState, 
            sagaId
        );
        exception.addContext("currentState", currentState);
        exception.addContext("expectedState", expectedState);
        exception.addContext("reason", "INVALID_STATE");
        return exception;
    }
}