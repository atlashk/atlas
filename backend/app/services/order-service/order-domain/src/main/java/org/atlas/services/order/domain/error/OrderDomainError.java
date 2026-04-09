package org.atlas.services.order.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.error.DomainError;

@Getter
@RequiredArgsConstructor
public enum OrderDomainError implements DomainError {

  // Cart-related errors
  CART_NOT_FOUND(4000, "error.cart.not_found"),
  CART_ITEM_NOT_FOUND(4001, "error.cart.item_not_found"),
  CART_ITEM_ALREADY_EXISTS(4002, "error.cart.item_already_exists"),
  CART_EMPTY(4003, "error.cart.empty"),

  // Order-related errors
  ORDER_NOT_FOUND(4100, "error.order.not_found"),
  FAILED_TO_PLACE_ORDER(4101, "error.order.failed_to_place_order"),
  ORDER_INVALID_STATUS(4102, "error.order.invalid_status"),
  ;

  private final int errorCode;
  private final String messageCode;
}
