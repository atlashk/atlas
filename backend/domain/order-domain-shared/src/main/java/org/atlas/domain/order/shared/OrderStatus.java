package org.atlas.domain.order.shared;

public enum OrderStatus {

  AWAITING_PRODUCT_RESERVATION,
  PRODUCT_RESERVATION_SUCCEEDED,
  AWAITING_PAYMENT,
  FULFILLED,
  CANCELED,
}
