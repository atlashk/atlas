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
@Schema(description = "Represents a brand in the brand list.")
public class BrandResponse {

  @Schema(description = "Unique identifier of the brand.", example = "1")
  private Integer id;

  @Schema(description = "Name of the brand.", example = "Brand Name")
  private String name;
}
