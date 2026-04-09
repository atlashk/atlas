package org.atlas.services.inventory.domain.entity;

/**
 * Represents the lifecycle state of a stock reservation within the inventory service.
 * <p>
 * A reservation is created when stock is temporarily held for an order during the checkout process.
 * <p>
 * State transitions:
 * <p>
 * ACTIVE
 *   ├── (payment success)  → CONFIRMED
 *   ├── (payment failure)  → RELEASED
 *   └── (timeout reached)  → EXPIRED
 * <p>
 * Once a reservation moves out of ACTIVE state, it must not transition to another state.
 */
public enum ReservationStatus {

    /**
     * Stock has been successfully reserved for an order.
     * <p>
     * The reserved quantity is deducted from the sellable stock
     * but not yet permanently deducted from available stock.
     * <p>
     * The reservation is waiting for payment confirmation.
     */
    ACTIVE,

    /**
     * The reservation has been confirmed after successful payment.
     * <p>
     * The reserved quantity is permanently deducted from stock.
     * This state is final and should not transition further.
     */
    CONFIRMED,

    /**
     * The reservation has been explicitly released due to
     * payment failure, order cancellation, or saga compensation.
     * <p>
     * The reserved quantity is returned to available stock.
     * This state is final.
     */
    RELEASED,

    /**
     * The reservation has expired because the payment was not
     * completed within the allowed time window.
     * <p>
     * The reserved quantity is automatically returned to stock.
     * This state is final.
     */
    EXPIRED
}
