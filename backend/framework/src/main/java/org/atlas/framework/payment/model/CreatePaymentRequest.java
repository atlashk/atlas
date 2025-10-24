package org.atlas.framework.payment.model;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CreatePaymentRequest {

  private Integer paymentId;
  private BigDecimal amount;
  private String currency;
}
