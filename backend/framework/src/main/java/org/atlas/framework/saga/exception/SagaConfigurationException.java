package org.atlas.framework.saga.exception;

import java.util.Map;

/**
 * Exception thrown when saga configuration is invalid or missing.
 */
public class SagaConfigurationException extends SagaException {

    private final String orchestratorName;
    private final String configurationKey;

    public SagaConfigurationException(String message) {
        super(message, null, "SAGA_CONFIGURATION_ERROR", false);
        this.orchestratorName = null;
        this.configurationKey = null;
    }

    public SagaConfigurationException(String message, String orchestratorName) {
        super(message, null, "SAGA_CONFIGURATION_ERROR", false);
        this.orchestratorName = orchestratorName;
        this.configurationKey = null;
    }

    public SagaConfigurationException(String message, String orchestratorName, 
                                    String configurationKey) {
        super(message, null, "SAGA_CONFIGURATION_ERROR", false);
        this.orchestratorName = orchestratorName;
        this.configurationKey = configurationKey;
    }

    public SagaConfigurationException(String message, Throwable cause, String orchestratorName, 
                                    String configurationKey, Map<String, Object> context) {
        super(message, cause, null, "SAGA_CONFIGURATION_ERROR", false, context);
        this.orchestratorName = orchestratorName;
        this.configurationKey = configurationKey;
    }

    // Getters
    public String getOrchestratorName() {
        return orchestratorName;
    }

    public String getConfigurationKey() {
        return configurationKey;
    }

    @Override
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Saga Configuration Exception");
        
        if (orchestratorName != null) {
            sb.append(" [Orchestrator: ").append(orchestratorName).append("]");
        }
        
        if (configurationKey != null) {
            sb.append(" [Configuration Key: ").append(configurationKey).append("]");
        }
        
        sb.append(" [Timestamp: ").append(getTimestamp()).append("]");
        sb.append(": ").append(getMessage());
        
        if (!getContext().isEmpty()) {
            sb.append(" [Context: ").append(getContext()).append("]");
        }
        
        return sb.toString();
    }

    // Static factory methods
    public static SagaConfigurationException orchestratorNotRegistered(String orchestratorName) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Orchestrator not registered: " + orchestratorName, orchestratorName
        );
        exception.addContext("reason", "ORCHESTRATOR_NOT_REGISTERED");
        return exception;
    }

    public static SagaConfigurationException invalidStepConfiguration(String orchestratorName, 
                                                                     String stepName, String reason) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Invalid step configuration for '" + stepName + "': " + reason, orchestratorName
        );
        exception.addContext("reason", "INVALID_STEP_CONFIGURATION");
        exception.addContext("stepName", stepName);
        exception.addContext("validationError", reason);
        return exception;
    }

    public static SagaConfigurationException missingAnnotation(String orchestratorName, 
                                                              String annotationType) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Missing required annotation '" + annotationType + "' on orchestrator: " + orchestratorName, 
            orchestratorName
        );
        exception.addContext("reason", "MISSING_ANNOTATION");
        exception.addContext("annotationType", annotationType);
        return exception;
    }

    public static SagaConfigurationException duplicateStepOrder(String orchestratorName, 
                                                               int stepOrder, String step1, String step2) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Duplicate step order " + stepOrder + " found in steps: " + step1 + ", " + step2, 
            orchestratorName
        );
        exception.addContext("reason", "DUPLICATE_STEP_ORDER");
        exception.addContext("stepOrder", stepOrder);
        exception.addContext("conflictingSteps", new String[]{step1, step2});
        return exception;
    }

    public static SagaConfigurationException invalidTimeout(String orchestratorName, 
                                                           String stepName, long timeout) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Invalid timeout value " + timeout + " for step: " + stepName, orchestratorName
        );
        exception.addContext("reason", "INVALID_TIMEOUT");
        exception.addContext("stepName", stepName);
        exception.addContext("timeout", timeout);
        return exception;
    }

    public static SagaConfigurationException invalidRetryCount(String orchestratorName, 
                                                              String stepName, int retryCount) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Invalid retry count " + retryCount + " for step: " + stepName, orchestratorName
        );
        exception.addContext("reason", "INVALID_RETRY_COUNT");
        exception.addContext("stepName", stepName);
        exception.addContext("retryCount", retryCount);
        return exception;
    }

    public static SagaConfigurationException circularDependency(String orchestratorName, 
                                                               String dependencyChain) {
        SagaConfigurationException exception = new SagaConfigurationException(
            "Circular dependency detected in orchestrator: " + dependencyChain, orchestratorName
        );
        exception.addContext("reason", "CIRCULAR_DEPENDENCY");
        exception.addContext("dependencyChain", dependencyChain);
        return exception;
    }
}