package org.atlas.services.order.infrastructure.api.server.rest.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response object for cart information")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CartResponse {

  @Schema(description = "Cart ID", example = "1")
  private Integer id;

  @Schema(description = "List of items in the cart")
  private List<CartItem> cartItems;

  @Schema(description = "Total amount of the cart", example = "99.99")
  private BigDecimal totalAmount;

  @Schema(description = "Response object for cart item information")
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class CartItem {

    @Schema(description = "Product information")
    private Product product;

    @Schema(description = "Quantity of the product in cart", example = "2")
    private Integer quantity;
  }

  @Schema(description = "Product information")
  @NoArgsConstructor
  @AllArgsConstructor
  @Builder
  @Getter
  @Setter
  public static class Product {

    @Schema(description = "Product ID", example = "1")
    private String id;

    @Schema(description = "Product name", example = "iPhone 15")
    private String name;

    @Schema(description = "Product price", example = "24.99")
    private BigDecimal price;

    @Schema(description = "Product image URL", example = "https://example.com/image.jpg")
    private String image;
  }
}
