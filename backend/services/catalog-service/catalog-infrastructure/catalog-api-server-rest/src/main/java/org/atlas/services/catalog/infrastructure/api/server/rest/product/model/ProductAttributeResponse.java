package org.atlas.services.catalog.infrastructure.api.server.rest.product.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Attributes associated with the product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttributeResponse {

  @Schema(description = "Unique identifier of the product attribute", example = "1")
  private Integer id;

  @Schema(description = "Name of the product attribute", example = "Color")
  private String name;

  @Schema(description = "Value of the product attribute", example = "Red")
  private String value;
}
