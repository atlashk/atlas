package org.atlas.infrastructure.api.server.rest.adapter.product.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about the product.")
public class ProductDetailsResponse {

  @Schema(description = "Description of the product.", example = "A comfortable cotton t-shirt.")
  private String description;
}
