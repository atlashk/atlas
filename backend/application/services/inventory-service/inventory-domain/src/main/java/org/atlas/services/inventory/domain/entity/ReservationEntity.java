package org.atlas.services.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class ReservationEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private String orderId;

  private String productId;

  private Integer quantity;
  
  private ReservationStatus status;
}
