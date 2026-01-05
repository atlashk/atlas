package org.atlas.payment.domain.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.domain.payment.PaymentStatus;
import org.atlas.common.framework.domain.common.entity.DomainEntity;
import org.atlas.common.framework.payment.model.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Payment extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer userId;
  private Integer orderId;
  private Integer sagaId; // Support for webhook case
  private BigDecimal amount;
  private String currency;
  private Integer paymentGatewayId;
  private String paymentMethod;
  private String paymentMethodDetails;
  private PaymentStatus status;

  // External payment gateway information
  private String transactionId;
  private NextAction nextAction;
  private String error;
  private String cancellationReason;
}
