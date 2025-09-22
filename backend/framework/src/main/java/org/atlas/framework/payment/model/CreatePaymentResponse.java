package org.atlas.framework.payment.model;

import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.payment.model.nextaction.NextAction;

@Getter
@Setter
public class CreatePaymentResponse {

  private boolean success;
  private String transactionId;
  private NextAction nextAction;
  private String errorCode;
  private String errorMessage;
}
