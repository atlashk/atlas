package org.atlas.services.inventory.infrastructure.api.server.rest.model.internal;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RetrieveStockListRequest {

  @NotEmpty
  @Schema(description = "List of unique identifiers for the products to be retrieved", example = "[\"PRD0000001\", \"PRD0000002\"]", requiredMode = RequiredMode.REQUIRED)
  private List<String> productIds;
}
