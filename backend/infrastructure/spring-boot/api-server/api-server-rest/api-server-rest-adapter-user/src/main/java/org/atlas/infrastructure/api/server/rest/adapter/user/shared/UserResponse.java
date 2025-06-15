package org.atlas.infrastructure.api.server.rest.adapter.user.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.atlas.domain.user.shared.enums.Role;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Response containing a paginated list of users.")
public class UserResponse {

  @Schema(description = "Unique identifier of the user.", example = "1")
  private Integer id;

  @Schema(description = "Username of the user.", example = "johndoe")
  private String username;

  @Schema(description = "First name of the user.", example = "John")
  private String firstName;

  @Schema(description = "Last name of the user.", example = "Doe")
  private String lastName;

  @Schema(description = "Email address of the user.", example = "johndoe@example.com")
  private String email;

  @Schema(description = "Phone number of the user.", example = "+1234567890")
  private String phoneNumber;

  @Schema(description = "Role of the user.")
  private Role role;
}
