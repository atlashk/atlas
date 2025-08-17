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
@Schema(description = "Attributes associated with the product.")
public class ProductAttributeResponse {

  @Schema(description = "Unique identifier of the product attribute.", example = "1")
  private Integer id;

  @Schema(description = "Name of the product attribute.", example = "Color")
  private String name;

  @Schema(description = "Value of the product attribute.", example = "Red")
  private String value;
}
