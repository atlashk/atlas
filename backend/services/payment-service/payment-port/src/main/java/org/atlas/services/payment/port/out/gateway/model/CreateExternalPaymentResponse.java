package org.atlas.services.payment.port.out.gateway.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.payment.domain.entity.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateExternalPaymentResponse {

  private boolean success;
  private String transactionId;
  private NextAction nextAction;
  private String errorCode;
  private String errorMessage;
}
