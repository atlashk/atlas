package org.atlas.domain.order.usecase.front.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentMethod;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FrontCheckoutInput {

  @NotNull
  private Integer userId;

  @NotNull
  private PaymentMethod paymentMethod;
}
