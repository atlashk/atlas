package org.atlas.services.payment.domain.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.error.DomainError;

@Getter
@RequiredArgsConstructor
public enum PaymentDomainError implements DomainError {

  // Payment-related errors
  PAYMENT_NOT_FOUND(5000, "error.payment.payment_not_found"),
  INVALID_PAYMENT_STATUS(5001, "error.payment.invalid_status"),
  PAYMENT_GATEWAY_NOT_FOUND(5002, "error.payment.payment_gateway_not_found"),
  ;

  private final int errorCode;
  private final String messageCode;
}
