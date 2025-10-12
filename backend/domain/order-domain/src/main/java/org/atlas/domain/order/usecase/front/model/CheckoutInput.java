package org.atlas.domain.order.usecase.front.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentMethod;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CheckoutInput {

  private Integer userId;
  private PaymentMethod paymentMethod;
}
