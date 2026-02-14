package org.atlas.services.iam.infrastructure.api.server.rest.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;

@Schema(description = "Request object for updating a user")
@Getter
@Setter
public class AdminUpdateUserRequest {

  @Schema(description = "Updated first name of the user", example = "John")
  private String firstName;

  @Schema(description = "Updated last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Updated role of the user", example = "USER")
  private UserRole role;
}
