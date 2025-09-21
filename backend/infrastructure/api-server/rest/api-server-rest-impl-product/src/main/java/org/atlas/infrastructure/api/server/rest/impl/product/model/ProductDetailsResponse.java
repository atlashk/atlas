package org.atlas.infrastructure.api.server.rest.impl.product.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Detailed information about the product.")
public class ProductDetailsResponse {

  @Schema(description = "Description of the product.", example = "A comfortable cotton t-shirt.")
  private String description;
}
