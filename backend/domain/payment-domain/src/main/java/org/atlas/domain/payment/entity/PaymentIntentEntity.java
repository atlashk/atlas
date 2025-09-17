package org.atlas.domain.payment.entity;

import java.math.BigDecimal;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentIntentEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private String stripePaymentIntentId;
  private Integer orderId;
  private Integer userId;
  private BigDecimal amount;
  private String currency = "USD";
  private String status;
  private String clientSecret;
  private String description;
  private String metadata;
}
