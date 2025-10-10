package org.atlas.framework.paymentgateway.exception;

public class PaymentGatewayException extends RuntimeException {

  public PaymentGatewayException(String message) {
    super(message);
  }

  public PaymentGatewayException(Throwable cause) {
    super(cause);
  }
}
