package org.atlas.framework.internalapi.user.model;

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
public class CartItemResponse {

  private Product product;
  private Integer quantity;

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
