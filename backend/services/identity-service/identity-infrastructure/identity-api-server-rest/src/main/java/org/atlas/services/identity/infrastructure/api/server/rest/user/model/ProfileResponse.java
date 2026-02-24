package org.atlas.services.identity.infrastructure.api.server.rest.user.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@Schema(description = "Response object for retrieving user info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ProfileResponse {

  @Schema(description = "Unique identifier of the user", example = "1")
  private String userId;

  @Schema(description = "Username of the user", example = "john_doe")
  private String username;

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Role of the user")
  private UserRole role;
}
