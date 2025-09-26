package org.atlas.infrastructure.api.server.rest.impl.user.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request object for adding item to cart")
@Getter
@Setter
public class FrontAddCartItemRequest {

  @NotNull
  @Schema(description = "Product ID to add to cart", example = "1", requiredMode = RequiredMode.REQUIRED)
  private Integer productId;

  @NotNull
  @Min(1)
  @Schema(description = "Quantity of the product", example = "2", requiredMode = RequiredMode.REQUIRED)
  private Integer quantity;
}
