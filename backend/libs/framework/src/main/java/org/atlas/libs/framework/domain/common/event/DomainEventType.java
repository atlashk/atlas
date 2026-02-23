package org.atlas.libs.framework.domain.common.event;

public enum DomainEventType {

  // Identity event types
  USER_CREATED,
  USER_UPDATED,
  USER_DELETED,

  // Catalog event types
  PRODUCT_CREATED,
  PRODUCT_UPDATED,
  PRODUCT_DELETED,

  // Inventory event types
  STOCK_STATUS_CHANGED,
}
