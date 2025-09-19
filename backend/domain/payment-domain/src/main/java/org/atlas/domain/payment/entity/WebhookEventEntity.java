package org.atlas.domain.payment.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class WebhookEventEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer paymentId;
  private Integer userId;
  private Integer orderId;
  private String transactionId;
  private String eventData;
  private Boolean processed = false;
  private String errorMessage;
  private Integer retryCount = 0;
}
