package org.atlas.services.inventory.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.error.DomainError;

@Getter
@RequiredArgsConstructor
public enum InventoryDomainError implements DomainError {

  // Stock-related errors
  STOCK_NOT_FOUND(3000, "error.stock.not_found"),

  // Inventory-related errors
  RESERVATION_NOT_FOUND(3100, "error.reservation.not_found"),
  ;

  private final int errorCode;
  private final String messageCode;
}
