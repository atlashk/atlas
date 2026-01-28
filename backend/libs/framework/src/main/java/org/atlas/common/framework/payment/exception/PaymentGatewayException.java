package org.atlas.common.framework.payment.exception;

public class PaymentGatewayException extends RuntimeException {

  public PaymentGatewayException(String message) {
    super(message);
  }

  public PaymentGatewayException(Throwable cause) {
    super(cause);
  }
}
