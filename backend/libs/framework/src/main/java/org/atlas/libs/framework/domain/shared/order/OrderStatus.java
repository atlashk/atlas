package org.atlas.libs.framework.domain.shared.order;

public enum OrderStatus {

  AWAITING_PRODUCT_RESERVATION,
  AWAITING_PAYMENT_INITIALIZED,
  AWAITING_PAYMENT_PROCESSED,
  FULFILLED,
  CANCELED,
}
