package org.atlas.services.payment.port.in.model;

import java.math.BigDecimal;
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
public class CreatePaymentInput {

  private String userId;

  private String orderId;

  private Integer sagaId;

  private BigDecimal amount;

  private String currency;

  private Integer paymentGatewayId;

  private PaymentStatus status;
}
