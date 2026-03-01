package org.atlas.services.payment.port.out.gateway.model;

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
public class CreateExternalPaymentRequest {

  private String paymentId;
  private BigDecimal amount;
  private String currency;
}
