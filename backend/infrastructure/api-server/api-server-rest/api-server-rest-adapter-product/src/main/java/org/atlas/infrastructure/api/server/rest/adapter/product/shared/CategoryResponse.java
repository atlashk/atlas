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
@Schema(description = "Represents a category in the category list.")
public class CategoryResponse {

  @Schema(description = "Unique identifier of the category.", example = "1")
  private Integer id;

  @Schema(description = "Name of the category.", example = "Category Name")
  private String name;
}
