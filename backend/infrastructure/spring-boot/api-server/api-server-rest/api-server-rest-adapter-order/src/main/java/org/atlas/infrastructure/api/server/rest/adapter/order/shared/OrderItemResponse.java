package org.atlas.infrastructure.api.server.rest.adapter.order.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents an item in an order")
public class OrderItemResponse {

  @Schema(description = "Product associated with the order item")
  private ProductResponse product;

  @Schema(description = "Quantity of the product in the order", example = "2")
  private Integer quantity;
}
