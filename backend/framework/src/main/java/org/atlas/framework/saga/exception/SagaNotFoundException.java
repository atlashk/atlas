package org.atlas.framework.saga.exception;

/**
 * Exception thrown when a saga cannot be found.
 */
public class SagaNotFoundException extends SagaException {

    public SagaNotFoundException(String sagaId) {
        super("Saga not found with ID: " + sagaId, sagaId, "SAGA_NOT_FOUND");
        addContext("reason", "SAGA_NOT_FOUND");
    }

    public SagaNotFoundException(String sagaId, String additionalInfo) {
        super("Saga not found with ID: " + sagaId + ". " + additionalInfo, 
              sagaId, "SAGA_NOT_FOUND");
        addContext("reason", "SAGA_NOT_FOUND");
        addContext("additionalInfo", additionalInfo);
    }

    public static SagaNotFoundException byId(String sagaId) {
        return new SagaNotFoundException(sagaId);
    }

    public static SagaNotFoundException byIdWithInfo(String sagaId, String info) {
        return new SagaNotFoundException(sagaId, info);
    }
}