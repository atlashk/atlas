package org.atlas.services.catalog.api.rest.category.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Represents a category in the category list")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {

  @Schema(description = "Unique identifier of the category", example = "CAT0001")
  private String id;

  @Schema(description = "Name of the category", example = "LAPTOP")
  private String name;
}
