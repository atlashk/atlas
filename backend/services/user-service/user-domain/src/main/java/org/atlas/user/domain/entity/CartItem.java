package org.atlas.user.domain.entity;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CartItem {

  private Integer id;
  private Product product;
  private Integer quantity;

  public BigDecimal getAmount() {
    return product.getPrice().multiply(BigDecimal.valueOf(quantity));
  }

  @NoArgsConstructor
  @Getter
  @Setter
  public static class Product {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;

    public Product(Integer id) {
      this.id = id;
    }
  }
}