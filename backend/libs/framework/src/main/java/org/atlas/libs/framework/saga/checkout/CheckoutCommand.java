package org.atlas.libs.framework.saga.checkout;

public class CheckoutCommand {

  public static final String RESERVE_PRODUCT = "RESERVE_PRODUCT";
  public static final String INITIALIZE_PAYMENT = "INITIALIZE_PAYMENT";
  public static final String PROCESS_PAYMENT = "PROCESS_PAYMENT";
  public static final String CLEAR_CART = "CLEAR_CART";
  public static final String NOTIFY_ORDER_FULFILLED = "NOTIFY_ORDER_FULFILLED";
}
