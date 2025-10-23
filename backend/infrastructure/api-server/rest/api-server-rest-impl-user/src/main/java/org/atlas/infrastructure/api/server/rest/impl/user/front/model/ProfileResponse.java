package org.atlas.infrastructure.api.server.rest.impl.user.front.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@Schema(description = "Response object for retrieving user info")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class ProfileResponse {

  @Schema(description = "Unique identifier of the user", example = "1")
  private Integer id;

  @Schema(description = "Username of the user", example = "john_doe")
  private String username;

  @Schema(description = "First name of the user", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user", example = "Doe")
  private String lastName;

  @Schema(description = "Email address of the user", example = "johndoe@example.com")
  private String email;

  @Schema(description = "Phone number of the user", example = "+1234567890")
  private String phoneNumber;

  @Schema(description = "Role of the user")
  private Role role;
}
