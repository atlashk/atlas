package org.atlas.framework.domain.event;

public enum DomainEventType {

  // User event types
  USER_REGISTERED,

  // Product event types
  PRODUCT_CREATED,
  PRODUCT_UPDATED,
  PRODUCT_DELETED,
  PRODUCT_RESERVATION_SUCCEEDED,
  PRODUCT_RESERVATION_FAILED,

  // Order event types
  ORDER_CREATED,
  ORDER_FULFILLED,
  ORDER_CANCELED,

  // Payment event types
  PAYMENT_CREATED,
  PAYMENT_SUCCEEDED,
  PAYMENT_FAILED,
  PAYMENT_CANCELED,
}
