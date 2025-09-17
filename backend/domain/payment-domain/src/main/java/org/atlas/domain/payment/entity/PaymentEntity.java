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
  private Integer paymentIntentId;
  private String stripeChargeId;
  private Integer orderId;
  private Integer userId;
  private BigDecimal amount;
  private String status;
  private String paymentMethodType;
  private String cardBrand;
  private String cardLast4;
  private String failureCode;
  private String failureMessage;
  private String receiptUrl;
}
