package org.atlas.infrastructure.api.server.rest.impl.user.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request object for updating cart item quantity")
@Getter
@Setter
public class UpdateCartItemRequest {

  @NotNull
  @Min(1)
  @Schema(description = "New quantity for the cart item", example = "3", requiredMode = RequiredMode.REQUIRED)
  private Integer quantity;
}