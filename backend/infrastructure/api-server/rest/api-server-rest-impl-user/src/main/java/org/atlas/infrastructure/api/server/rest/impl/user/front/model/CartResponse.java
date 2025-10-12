package org.atlas.infrastructure.api.server.rest.impl.user.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Response object for cart information")
@Getter
@Setter
public class CartResponse {

  @Schema(description = "Cart ID", example = "1")
  private Integer id;

  @Schema(description = "List of items in the cart")
  private List<CartItemResponse> cartItems;

  @Schema(description = "Total amount of the cart", example = "99.99")
  private BigDecimal totalAmount;
}
