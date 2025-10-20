package org.atlas.domain.payment.entity;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.PaymentGateway;
import org.atlas.domain.payment.shared.PaymentMethod;
import org.atlas.domain.payment.shared.PaymentStatus;
import org.atlas.framework.domain.entity.DomainEntity;
import org.atlas.framework.payment.model.nextaction.NextAction;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer userId;
  private Integer orderId;
  private BigDecimal amount;
  private String currency;
  private PaymentMethod method;
  private PaymentGateway gateway;
  private PaymentStatus status;

  // External payment gateway information
  private String transactionId;
  private NextAction nextAction;
  private String errorCode;
  private String errorMessage;
  private String cancellationReason;
}
