package org.atlas.services.payment.port.in.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.services.payment.domain.entity.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UpdatePaymentInput {

  private String id;

  private String paymentMethod;

  private String paymentMethodDetails;

  private PaymentStatus status;

  private String transactionId;

  private NextAction nextAction;

  private String error;

  private String cancellationReason;
}
