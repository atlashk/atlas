package org.atlas.services.order.domain.entity;

import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;
import org.atlas.libs.framework.util.CollectionUtil;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class CartItem extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

  private String userId;

  private Product product;

  private Integer quantity;

  public BigDecimal getAmount() {
    if (product == null || product.getPrice() == null || quantity == null) {
      return BigDecimal.ZERO;
    }
    return product.getPrice().multiply(BigDecimal.valueOf(quantity));
  }

  public static BigDecimal totalAmount(List<CartItem> cartItems) {
    if (CollectionUtil.isEmpty(cartItems)) {
      return BigDecimal.ZERO;
    }
    return cartItems.stream()
        .map(CartItem::getAmount)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Product {

    private String id;
    private String name;
    private BigDecimal price;
    private String image;

    public Product(String id) {
      this.id = id;
    }
  }
}
