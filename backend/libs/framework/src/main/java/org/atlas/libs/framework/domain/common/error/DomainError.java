package org.atlas.libs.framework.domain.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.http.HttpStatusCode;

@Getter
@RequiredArgsConstructor
public enum DomainError {

  // Common errors
  DEFAULT(HttpStatusCode.INTERNAL_SERVER_ERROR.getCode(), "error.commons.default"),
  BAD_REQUEST(HttpStatusCode.BAD_REQUEST.getCode(), "error.commons.bad_request"),
  UNAUTHORIZED(HttpStatusCode.UNAUTHORIZED.getCode(), "error.commons.unauthorized"),
  FORBIDDEN(HttpStatusCode.FORBIDDEN.getCode(), "error.commons.permission_denied"),
  NOT_FOUND(HttpStatusCode.NOT_FOUND.getCode(), "error.commons.not_found"),
  CONFLICT(HttpStatusCode.CONFLICT.getCode(), "error.commons.conflict"),

  // User-related errors
  USER_NOT_FOUND(1000, "error.user.not_found"),
  USERNAME_ALREADY_EXISTS(1001, "error.user.username_already_exists"),
  EMAIL_ALREADY_EXISTS(1002, "error.user.email_already_exists"),
  PHONE_NUMBER_ALREADY_EXISTS(1003, "error.user.phone_number_already_exists"),

  // Product-related errors
  PRODUCT_NOT_FOUND(2000, "error.product.not_found"),
  BRAND_NOT_FOUND(2001, "error.product.brand.not_found"),
  CATEGORY_NOT_FOUND(2002, "error.product.category.not_found"),
  NO_IMPORTED_PRODUCT(2003, "error.product.no_imported_product"),
  FAILED_TO_IMPORT_PRODUCT(2004, "error.product.failed_to_import_product"),
  RESERVATION_NOT_FOUND(2005, "error.product.reservation.not_found"),

  // Order-related errors
  ORDER_NOT_FOUND(3000, "error.order.not_found"),
  FAILED_TO_PLACE_ORDER(3001, "error.order.failed_to_place_order"),
  ORDER_INVALID_STATUS(3002, "error.order.invalid_status"),

  // Payment-related errors
  PAYMENT_NOT_FOUND(4000, "error.payment.payment_not_found"),
  INVALID_PAYMENT_STATUS(4001, "error.payment.invalid_status"),
  PAYMENT_GATEWAY_NOT_FOUND(4002, "error.payment.payment_gateway_not_found"),

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
