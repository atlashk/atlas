package org.atlas.domain.user.entity;

import java.math.BigDecimal;
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
@EqualsAndHashCode(callSuper = true)
public class CartItemEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private ProductEntity product;
  private Integer quantity;

  public BigDecimal getAmount() {
    return product.getPrice().multiply(BigDecimal.valueOf(quantity));
  }
}
