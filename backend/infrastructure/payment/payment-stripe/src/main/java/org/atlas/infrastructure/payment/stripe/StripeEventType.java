package org.atlas.infrastructure.payment.stripe;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StripeEventType {

  public static final String PAYMENT_INTENT_SUCCEEDED = "payment_intent.succeeded";
  public static final String PAYMENT_INTENT_PAYMENT_FAILED = "payment_intent.payment_failed";
  public static final String PAYMENT_INTENT_CANCELED = "payment_intent.canceled";
}
