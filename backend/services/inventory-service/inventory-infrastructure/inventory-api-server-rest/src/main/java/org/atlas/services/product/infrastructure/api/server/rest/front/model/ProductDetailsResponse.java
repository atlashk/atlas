package org.atlas.services.product.infrastructure.api.server.rest.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Detailed information about the product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDetailsResponse {

  @Schema(description = "Description of the product", example = "A comfortable cotton T-shirt")
  private String description;
}
