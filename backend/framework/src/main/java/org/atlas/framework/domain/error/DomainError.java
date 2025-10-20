package org.atlas.framework.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum DomainError {

  // Common errors
  DEFAULT(500, "error.commons.default"),
  BAD_REQUEST(400, "error.commons.bad_request"),
  UNAUTHORIZED(401, "error.commons.unauthorized"),
  FORBIDDEN(403, "error.commons.permission_denied"),
  NOT_FOUND(404, "error.commons.not_found"),
  CONFLICT(409, "error.commons.conflict"),

  // User-related errors
  USER_NOT_FOUND(1000, "error.user.not_found"),
  USERNAME_ALREADY_EXISTS(1001, "error.user.username_already_exists"),
  EMAIL_ALREADY_EXISTS(1002, "error.user.email_already_exists"),
  PHONE_NUMBER_ALREADY_EXISTS(1003, "error.user.phone_number_already_exists"),

  // Product-related errors
  PRODUCT_NOT_FOUND(2000, "error.product.not_found"),
  PRODUCT_INSUFFICIENT_QUANTITY(2001, "error.product.insufficient_quantity"),
  BRAND_NOT_FOUND(2002, "error.product.brand.not_found"),
  CATEGORY_NOT_FOUND(2003, "error.product.category.not_found"),
  NO_IMPORTED_PRODUCT(2004, "error.product.no_imported_product"),
  FAILED_TO_IMPORT_PRODUCT(2005, "error.product.failed_to_import_product"),
  RESERVATION_NOT_FOUND(2006, "error.product.reservation.not_found"),

  // Order-related errors
  ORDER_NOT_FOUND(3000, "error.order.not_found"),
  FAILED_TO_PLACE_ORDER(3001, "error.order.failed_to_place_order"),
  ORDER_INVALID_STATUS(3002, "error.order.invalid_status"),

  // Payment-related errors
  PAYMENT_NOT_FOUND(4000, "error.payment.payment_not_found"),
  INVALID_PAYMENT_STATUS(4001, "error.payment.invalid_status"),
  PAYMENT_GATEWAY_NOT_SUPPORTED(4002, "error.payment.payment_gateway_not_supported"),
  PAYMENT_METHOD_NOT_SUPPORTED(4003, "error.payment.payment_method_not_supported"),

  // Cart-related errors
  CART_NOT_FOUND(5000, "error.cart.not_found"),
  CART_ITEM_NOT_FOUND(5001, "error.cart.item_not_found"),
  CART_ITEM_ALREADY_EXISTS(5002, "error.cart.item_already_exists"),
  CART_EMPTY(5003, "error.cart.empty"),
  ;

  private final int errorCode;
  private final String messageCode;

  @Override
  public String toString() {
    return String.format("%d %s", errorCode, name());
  }
}
