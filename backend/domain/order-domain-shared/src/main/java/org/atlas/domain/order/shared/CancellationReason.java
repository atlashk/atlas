package org.atlas.domain.order.shared;

import lombok.experimental.UtilityClass;

@UtilityClass
public class CancellationReason {

  public static final String PRODUCT_RESERVATION_FAILED = "Product reservation failed";
  public static final String PAYMENT_FAILED = "Payment failed";
  public static final String PAYMENT_CANCELED = "Payment canceled";
}
