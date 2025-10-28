package org.atlas.domain.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class PaymentEvent extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer paymentGatewayId;
  private Integer paymentId;
  private String payload;
  private String headers;
  private PaymentEventStatus status;
  private String error;
}
