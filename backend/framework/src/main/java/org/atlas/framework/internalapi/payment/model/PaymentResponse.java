package org.atlas.framework.internalapi.payment.model;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentStatus;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {

  private Integer paymentId;
  private Integer orderId;
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
