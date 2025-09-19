package org.atlas.domain.payment.entity;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.payment.shared.enums.PaymentStatus;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer userId;
  private Integer orderId;
  private String transactionId; // External payment gateway transaction ID
  private BigDecimal amount;
  private String currency;
  private PaymentStatus status;
  private String errorCode;
  private String errorMessage;
  private String receiptUrl;
}
