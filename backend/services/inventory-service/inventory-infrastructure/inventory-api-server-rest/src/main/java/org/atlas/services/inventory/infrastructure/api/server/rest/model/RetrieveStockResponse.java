package org.atlas.services.inventory.infrastructure.api.server.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Response object containing stock information for a product")
public class RetrieveStockResponse {

  @Schema(description = "Product ID", example = "123")
  private String productId;

  @Schema(description = "Available quantity in stock", example = "100")
  private Integer availableQuantity;

  @Schema(description = "Reserved quantity (held for pending orders)", example = "10")
  private Integer reservedQuantity;
}

