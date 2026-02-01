package org.atlas.services.iam.infrastructure.api.server.rest.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request object for listing users by their IDs")
@Getter
@Setter
public class InternalRetrieveUserListRequest {

  @NotEmpty
  @Schema(description = "List of unique identifiers for the users to be retrieved", example = "[1,2,3,4]")
  private List<Integer> ids;
}
