package org.atlas.framework.paymentgateway.model;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.paymentgateway.model.nextaction.NextAction;

@Getter
@Setter
public class CreatePaymentResponse {

  private boolean success;
  private String transactionId;
  private NextAction nextAction;
  private String errorCode;
  private String errorMessage;
}
