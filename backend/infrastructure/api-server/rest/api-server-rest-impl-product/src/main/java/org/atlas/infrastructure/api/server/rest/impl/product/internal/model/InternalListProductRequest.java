package org.atlas.infrastructure.api.server.rest.impl.product.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InternalListProductRequest {

  @NotEmpty
  @Schema(description = "List of unique identifiers for the products to be retrieved", example = "[1, 2, 3]", requiredMode = RequiredMode.REQUIRED)
  private List<Integer> ids;
}
