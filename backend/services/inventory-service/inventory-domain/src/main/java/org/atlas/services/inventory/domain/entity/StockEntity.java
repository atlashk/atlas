package org.atlas.services.inventory.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.entity.DomainEntity;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class StockEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String productId;   // same id as product

  @Builder.Default
  private Integer availableQuantity = 0;

  @Builder.Default
  private int reservedQuantity = 0;
}
