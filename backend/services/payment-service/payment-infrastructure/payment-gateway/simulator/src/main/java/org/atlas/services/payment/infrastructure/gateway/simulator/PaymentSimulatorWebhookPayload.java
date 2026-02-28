package org.atlas.services.payment.infrastructure.gateway.simulator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class PaymentSimulatorWebhookPayload {

  private String paymentId;
  private String paymentMethod;
  private String paymentMethodDetails;
  private PaymentStatus status;
  private String error;
  private String cancellationReason;
}
