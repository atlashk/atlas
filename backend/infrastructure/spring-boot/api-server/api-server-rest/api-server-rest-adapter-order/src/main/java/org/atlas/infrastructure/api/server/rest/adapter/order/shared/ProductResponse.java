package org.atlas.infrastructure.api.server.rest.adapter.order.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Represents a product in the order item")
public class ProductResponse {

  @Schema(description = "Unique identifier of the product", example = "1")
  private Integer id;

  @Schema(description = "Name of the product", example = "iPhone")
  private String name;

  @Schema(description = "Price of the product", example = "49.99")
  private BigDecimal price;

  @Schema(description = "Image of the product as Base64 string", example = "data:image/jpeg;base64,...")
  private String image;
}
