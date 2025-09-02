package org.atlas.framework.domain.event;

public enum DomainEventType {

  // User Service event types
  USER_REGISTERED,

  // Product Service event types
  PRODUCT_CREATED,
  PRODUCT_UPDATED,
  PRODUCT_DELETED,

  // Order Service event types
  ORDER_CREATED,
  RESERVE_QUANTITY_SUCCEEDED,
  RESERVE_QUANTITY_FAILED,
  ORDER_CONFIRMED,
  ORDER_CANCELED,
}
