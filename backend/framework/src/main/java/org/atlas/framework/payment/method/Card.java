package org.atlas.framework.payment.method;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Card implements PaymentMethodDetails {

  private String brand;
  private String last4;
}
