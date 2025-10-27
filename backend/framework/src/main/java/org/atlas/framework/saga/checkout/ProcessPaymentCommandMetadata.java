package org.atlas.framework.saga.checkout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentStatus;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProcessPaymentCommandMetadata {

  private PaymentStatus paymentStatus;
  private String paymentMethod;
  private String paymentMethodDetails;
}
