package org.atlas.services.inventory.infrastructure.api.server.rest.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
@Schema(description = "Request object for updating available quantity of a product")
public class UpdateAvailableQuantityRequest {

  @Schema(description = "New available quantity to set", example = "50", requiredMode = RequiredMode.REQUIRED)
  @NotNull(message = "Available quantity is required")
  @PositiveOrZero(message = "Available quantity must be zero or positive")
  private Integer availableQuantity;
}

