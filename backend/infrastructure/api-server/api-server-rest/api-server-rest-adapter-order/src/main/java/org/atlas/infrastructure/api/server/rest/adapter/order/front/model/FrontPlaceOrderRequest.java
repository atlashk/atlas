package org.atlas.infrastructure.api.server.rest.adapter.order.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "Request object for placing a new order.")
public class FrontPlaceOrderRequest {

  @NotEmpty(message = "Order items must not be empty.")
  @Schema(description = "List of items to be ordered, must not be empty.", required = true)
  private List<@Valid OrderItem> orderItems;

  @Data
  @Schema(description = "Represents an item in the order.")
  public static class OrderItem {

    @NotNull(message = "Product ID must not be null.")
    @Schema(description = "ID of the product to order.", example = "123", required = true)
    private Integer productId;

    @NotNull(message = "Quantity must not be null.")
    @Min(value = 1, message = "Quantity must be at least 1.")
    @Schema(description = "Quantity of the product to order, must be at least 1.", example = "2", required = true)
    private Integer quantity;
  }
}
