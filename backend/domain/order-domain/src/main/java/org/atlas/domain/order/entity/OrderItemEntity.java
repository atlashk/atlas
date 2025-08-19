package org.atlas.domain.order.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.atlas.framework.domain.entity.DomainEntity;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class OrderItemEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer orderId;
  private ProductEntity product;
  private Integer quantity;
}
