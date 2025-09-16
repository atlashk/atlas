package org.atlas.framework.domain.event;

public enum DomainEventType {

  // User Service event types
  USER_REGISTERED,

  // Product Service event types
  PRODUCT_CREATED,
  PRODUCT_UPDATED,
  PRODUCT_DELETED,
  PRODUCT_RESERVE_QUANTITY_SUCCEEDED,
  PRODUCT_RESERVE_QUANTITY_FAILED,

  // Order Service event types
  ORDER_CREATED,
  ORDER_CONFIRMED,
  ORDER_CANCELED,

  // Payment Service event types
  PAYMENT_SUCCEEDED,
  PAYMENT_FAILED,
}
