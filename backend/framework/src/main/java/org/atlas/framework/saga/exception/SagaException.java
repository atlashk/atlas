package org.atlas.framework.saga.exception;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.HashMap;

/**
 * Base exception class for all saga-related exceptions.
 * Provides comprehensive error information including context and metadata.
 */
public class SagaException extends RuntimeException {

    private final String sagaId;
    private final String errorCode;
    private final LocalDateTime timestamp;
    private final Map<String, Object> context;
    private final boolean retryable;

    public SagaException(String message) {
        this(message, null, null, null, false, null);
    }

    public SagaException(String message, Throwable cause) {
        this(message, cause, null, null, false, null);
    }

    public SagaException(String message, String sagaId) {
        this(message, null, sagaId, null, false, null);
    }

    public SagaException(String message, String sagaId, String errorCode) {
        this(message, null, sagaId, errorCode, false, null);
    }

    public SagaException(String message, String sagaId, String errorCode, boolean retryable) {
        this(message, null, sagaId, errorCode, retryable, null);
    }

    public SagaException(String message, Throwable cause, String sagaId, String errorCode, 
                        boolean retryable, Map<String, Object> context) {
        super(message, cause);
        this.sagaId = sagaId;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.context = context != null ? new HashMap<>(context) : new HashMap<>();
        this.retryable = retryable;
    }

    // Getters
    public String getSagaId() {
        return sagaId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Map<String, Object> getContext() {
        return new HashMap<>(context);
    }

    public boolean isRetryable() {
        return retryable;
    }

    // Context management
    public void addContext(String key, Object value) {
        this.context.put(key, value);
    }

    public Object getContextValue(String key) {
        return this.context.get(key);
    }

    public boolean hasContext(String key) {
        return this.context.containsKey(key);
    }

    // Utility methods
    public String getFormattedMessage() {
        StringBuilder sb = new StringBuilder();
        sb.append("Saga Exception");
        
        if (sagaId != null) {
            sb.append(" [Saga ID: ").append(sagaId).append("]");
        }
        
        if (errorCode != null) {
            sb.append(" [Error Code: ").append(errorCode).append("]");
        }
        
        sb.append(" [Timestamp: ").append(timestamp).append("]");
        sb.append(" [Retryable: ").append(retryable).append("]");
        sb.append(": ").append(getMessage());
        
        if (!context.isEmpty()) {
            sb.append(" [Context: ").append(context).append("]");
        }
        
        return sb.toString();
    }

    @Override
    public String toString() {
        return getFormattedMessage();
    }

    // Builder pattern for complex exception creation
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String message;
        private Throwable cause;
        private String sagaId;
        private String errorCode;
        private boolean retryable = false;
        private Map<String, Object> context = new HashMap<>();

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder cause(Throwable cause) {
            this.cause = cause;
            return this;
        }

        public Builder sagaId(String sagaId) {
            this.sagaId = sagaId;
            return this;
        }

        public Builder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        public Builder retryable(boolean retryable) {
            this.retryable = retryable;
            return this;
        }

        public Builder context(String key, Object value) {
            this.context.put(key, value);
            return this;
        }

        public Builder context(Map<String, Object> context) {
            if (context != null) {
                this.context.putAll(context);
            }
            return this;
        }

        public SagaException build() {
            return new SagaException(message, cause, sagaId, errorCode, retryable, context);
        }
    }
}