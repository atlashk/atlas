package org.atlas.infrastructure.api.server.rest.adapter.user.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Data;

@Data
@Schema(description = "Request object for listing users by their IDs.")
public class InternalListUserRequest {

  @NotEmpty
  @Schema(description = "List of user IDs to retrieve.", example = "[1,2,3,4]")
  private List<Integer> ids;
}
