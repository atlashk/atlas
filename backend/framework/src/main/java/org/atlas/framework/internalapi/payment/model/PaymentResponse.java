package org.atlas.framework.internalapi.payment.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentGatewayCode;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.domain.payment.shared.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  private Integer id;
  private BigDecimal amount;
  private String currency;
  private PaymentMethod method;
  private PaymentGatewayCode gateway;
  private PaymentStatus status;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
