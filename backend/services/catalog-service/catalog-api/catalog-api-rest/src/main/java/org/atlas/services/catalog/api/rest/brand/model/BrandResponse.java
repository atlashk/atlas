package org.atlas.services.catalog.api.rest.brand.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Represents a brand in the brand list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandResponse {

  @Schema(description = "Unique identifier of the brand", example = "BRD0001")
  private String id;

  @Schema(description = "Name of the brand", example = "Brand Name")
  private String name;
}
