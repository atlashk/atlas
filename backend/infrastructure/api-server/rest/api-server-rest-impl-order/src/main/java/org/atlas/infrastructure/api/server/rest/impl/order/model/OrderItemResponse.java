package org.atlas.infrastructure.api.server.rest.impl.order.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Represents an item in an order")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {

  @Schema(description = "Product associated with the order item")
  private ProductResponse product;

  @Schema(description = "Quantity of the product in the order", example = "2")
  private Integer quantity;
}
