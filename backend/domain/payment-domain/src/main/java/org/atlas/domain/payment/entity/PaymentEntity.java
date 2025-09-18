package org.atlas.domain.payment.entity;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer orderId;
  private Integer userId;
  private BigDecimal amount;
  private String currency;
  private String paymentMethodType;
  private PaymentStatus status;
  private String error;
  private String receiptUrl;
}
