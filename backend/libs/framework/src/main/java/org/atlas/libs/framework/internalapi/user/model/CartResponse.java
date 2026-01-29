package org.atlas.libs.framework.internalapi.user.model;

import java.math.BigDecimal;
import java.util.List;
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
public class CartResponse {

  private List<CartItem> cartItems;

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class CartItem {

    private Product product;
    private Integer quantity;
  }

  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Product {

    private Integer id;
    private String name;
    private BigDecimal price;
    private String image;
  }
}
