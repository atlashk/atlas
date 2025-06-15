package org.atlas.infrastructure.api.server.rest.adapter.product.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
public class InternalListProductRequest {

  @NotEmpty
  @Schema(
      description = "List of unique identifiers (IDs) for the products to be retrieved.",
      requiredMode = RequiredMode.REQUIRED,
      example = "[1, 2, 3]"
  )
  private List<Integer> ids;
}
