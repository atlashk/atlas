package org.atlas.domain.payment.usecase.front.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.paymentgateway.model.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GetPaymentTrackingOutput {

  private PaymentStatus status;
  private String transactionId;
  private NextAction nextAction;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
