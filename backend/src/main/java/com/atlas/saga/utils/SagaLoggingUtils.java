package com.atlas.saga.utils;

import com.atlas.saga.entity.SagaEntity;
import com.atlas.saga.enums.SagaStatus;
import com.atlas.saga.enums.StepStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Utility class for saga logging and monitoring in event-driven architecture
 */
public class SagaLoggingUtils {
    
    private static final Logger logger = LoggerFactory.getLogger(SagaLoggingUtils.class);
    private static final String SAGA_ID_KEY = "sagaId";
    private static final String ORCHESTRATOR_KEY = "orchestrator";
    private static final String STEP_NAME_KEY = "stepName";
    private static final String STEP_ORDER_KEY = "stepOrder";
    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    
    /**
     * Log saga started event
     */
    public static void logSagaStarted(SagaEntity saga) {
        try {
            MDC.put(SAGA_ID_KEY, saga.getSagaId());
            MDC.put(ORCHESTRATOR_KEY, saga.getOrchestratorName());
            
            logger.info("SAGA_STARTED - Saga {} initiated by orchestrator {} at {}", 
                saga.getSagaId(), 
                saga.getOrchestratorName(),
                formatTimestamp(saga.getCreatedAt()));
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga completed event
     */
    public static void logSagaCompleted(SagaEntity saga) {
        try {
            MDC.put(SAGA_ID_KEY, saga.getSagaId());
            MDC.put(ORCHESTRATOR_KEY, saga.getOrchestratorName());
            
            logger.info("SAGA_COMPLETED - Saga {} completed successfully at {}", 
                saga.getSagaId(),
                formatTimestamp(saga.getUpdatedAt()));
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga failed event
     */
    public static void logSagaFailed(SagaEntity saga, String reason) {
        try {
            MDC.put(SAGA_ID_KEY, saga.getSagaId());
            MDC.put(ORCHESTRATOR_KEY, saga.getOrchestratorName());
            
            logger.error("SAGA_FAILED - Saga {} failed with reason: {} at {}", 
                saga.getSagaId(),
                reason,
                formatTimestamp(saga.getUpdatedAt()));
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga compensated event
     */
    public static void logSagaCompensated(SagaEntity saga) {
        try {
            MDC.put(SAGA_ID_KEY, saga.getSagaId());
            MDC.put(ORCHESTRATOR_KEY, saga.getOrchestratorName());
            
            logger.warn("SAGA_COMPENSATED - Saga {} compensation completed at {}", 
                saga.getSagaId(),
                formatTimestamp(saga.getUpdatedAt()));
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log step started event
     */
    public static void logStepStarted(String sagaId, String orchestratorName, String stepName, int stepOrder) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            MDC.put(STEP_NAME_KEY, stepName);
            MDC.put(STEP_ORDER_KEY, String.valueOf(stepOrder));
            
            logger.info("STEP_STARTED - Step {} (order: {}) started for saga {}", 
                stepName, stepOrder, sagaId);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log step completed event
     */
    public static void logStepCompleted(String sagaId, String orchestratorName, String stepName, int stepOrder, long durationMs) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            MDC.put(STEP_NAME_KEY, stepName);
            MDC.put(STEP_ORDER_KEY, String.valueOf(stepOrder));
            
            logger.info("STEP_COMPLETED - Step {} (order: {}) completed for saga {} in {}ms", 
                stepName, stepOrder, sagaId, durationMs);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log step failed event
     */
    public static void logStepFailed(String sagaId, String orchestratorName, String stepName, int stepOrder, String error) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            MDC.put(STEP_NAME_KEY, stepName);
            MDC.put(STEP_ORDER_KEY, String.valueOf(stepOrder));
            
            logger.error("STEP_FAILED - Step {} (order: {}) failed for saga {} with error: {}", 
                stepName, stepOrder, sagaId, error);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log step compensation event
     */
    public static void logStepCompensated(String sagaId, String orchestratorName, String stepName, int stepOrder) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            MDC.put(STEP_NAME_KEY, stepName);
            MDC.put(STEP_ORDER_KEY, String.valueOf(stepOrder));
            
            logger.warn("STEP_COMPENSATED - Step {} (order: {}) compensated for saga {}", 
                stepName, stepOrder, sagaId);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga status change
     */
    public static void logSagaStatusChange(String sagaId, String orchestratorName, SagaStatus oldStatus, SagaStatus newStatus) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            
            logger.info("SAGA_STATUS_CHANGE - Saga {} status changed from {} to {}", 
                sagaId, oldStatus, newStatus);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga retry attempt
     */
    public static void logSagaRetry(String sagaId, String orchestratorName, String stepName, int retryCount, String reason) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            MDC.put(STEP_NAME_KEY, stepName);
            
            logger.warn("SAGA_RETRY - Saga {} step {} retry attempt {} due to: {}", 
                sagaId, stepName, retryCount, reason);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga event with custom data
     */
    public static void logSagaEvent(String sagaId, String orchestratorName, String eventType, Map<String, Object> eventData) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            
            logger.info("SAGA_EVENT - Saga {} event {} with data: {}", 
                sagaId, eventType, eventData);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Log saga performance metrics
     */
    public static void logSagaMetrics(String sagaId, String orchestratorName, long totalDurationMs, int totalSteps, int completedSteps) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            
            logger.info("SAGA_METRICS - Saga {} completed {}/{} steps in {}ms", 
                sagaId, completedSteps, totalSteps, totalDurationMs);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Create structured log entry for saga monitoring
     */
    public static void logStructuredSagaEvent(String sagaId, String orchestratorName, String eventType, 
                                            String stepName, StepStatus stepStatus, Map<String, Object> metadata) {
        try {
            MDC.put(SAGA_ID_KEY, sagaId);
            MDC.put(ORCHESTRATOR_KEY, orchestratorName);
            if (stepName != null) {
                MDC.put(STEP_NAME_KEY, stepName);
            }
            
            logger.info("STRUCTURED_SAGA_EVENT - Type: {}, Step: {}, Status: {}, Metadata: {}", 
                eventType, stepName, stepStatus, metadata);
                
        } finally {
            clearMDC();
        }
    }
    
    /**
     * Format timestamp for consistent logging
     */
    private static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp != null ? timestamp.format(TIMESTAMP_FORMATTER) : "N/A";
    }
    
    /**
     * Clear MDC context
     */
    private static void clearMDC() {
        MDC.remove(SAGA_ID_KEY);
        MDC.remove(ORCHESTRATOR_KEY);
        MDC.remove(STEP_NAME_KEY);
        MDC.remove(STEP_ORDER_KEY);
    }
    
    /**
     * Set up MDC context for saga operations
     */
    public static void setupSagaContext(String sagaId, String orchestratorName) {
        MDC.put(SAGA_ID_KEY, sagaId);
        MDC.put(ORCHESTRATOR_KEY, orchestratorName);
    }
    
    /**
     * Clear all saga context from MDC
     */
    public static void clearSagaContext() {
        clearMDC();
    }
}