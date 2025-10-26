package org.atlas.domain.order.vo;

import java.math.BigDecimal;
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
public class PaymentVO {

  private Integer id;
  private String transactionId;
  private BigDecimal amount;
  private String currency;
  private String paymentGateway;
  private String paymentMethod;
  private String paymentMethodDetails;
  private PaymentStatus status;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
