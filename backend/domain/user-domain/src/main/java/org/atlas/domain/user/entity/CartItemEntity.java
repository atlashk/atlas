package org.atlas.domain.user.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class CartItemEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;
  private Integer cartId;
  private ProductEntity product;
  private Integer quantity;
}
