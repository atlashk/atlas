package org.atlas.framework.saga.command.model;

public class CheckoutCommand {

  public static final String CREATE_ORDER = "CREATE_ORDER";
  public static final String RESERVE_PRODUCT = "RESERVE_PRODUCT";
  public static final String INITIALIZE_PAYMENT = "INITIALIZE_PAYMENT";
  public static final String PROCESS_PAYMENT = "PROCESS_PAYMENT";
  public static final String CLEAR_CART = "CLEAR_CART";
}
