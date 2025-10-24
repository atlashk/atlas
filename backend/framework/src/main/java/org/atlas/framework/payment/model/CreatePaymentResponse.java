package org.atlas.framework.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.payment.model.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreatePaymentResponse {

  private boolean success;
  private String transactionId;
  private NextAction nextAction;
  private String errorCode;
  private String errorMessage;
}
