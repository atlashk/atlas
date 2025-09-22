package org.atlas.domain.order.entity;

import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.domain.payment.shared.PaymentStatus;

@Getter
@Setter
public class PaymentEntity {

  private Integer id;
  private String transactionId;
  private BigDecimal amount;
  private String currency;
  private PaymentMethod method;
  private PaymentGateway gateway;
  private PaymentStatus status;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
