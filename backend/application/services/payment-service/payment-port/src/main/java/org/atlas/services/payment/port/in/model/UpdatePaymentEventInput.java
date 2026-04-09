package org.atlas.services.payment.port.in.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.services.payment.domain.entity.PaymentEventStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UpdatePaymentEventInput {

  private Integer id;

  private String paymentId;

  private PaymentEventStatus status;

  private String error;
}
