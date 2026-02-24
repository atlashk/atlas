package org.atlas.services.payment.domain.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;
import org.atlas.libs.framework.domain.shared.payment.PaymentStatus;
import org.atlas.libs.framework.payment.model.nextaction.NextAction;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;
  private String userId;
  private String orderId;
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
