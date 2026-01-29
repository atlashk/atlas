package org.atlas.services.product.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.entity.DomainEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class Reservation extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer orderId;
  private Integer productId;
  private Integer quantity;
}
