package org.atlas.libs.framework.payment.model;

import java.math.BigDecimal;
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
public class CreatePaymentRequest {

  private Integer paymentId;
  private BigDecimal amount;
  private String currency;
}
