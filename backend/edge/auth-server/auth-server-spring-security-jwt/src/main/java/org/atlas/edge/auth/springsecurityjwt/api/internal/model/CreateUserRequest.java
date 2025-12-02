package org.atlas.edge.auth.springsecurityjwt.api.internal.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.constant.Patterns;

@Schema(description = "Request object for user registration containing required data")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateUserRequest {

  @NotNull
  @Schema(description = "User ID", example = "1", requiredMode = RequiredMode.REQUIRED)
  private Integer userId;

  @NotBlank
  @Schema(description = "Username for the new user", example = "john_doe", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotBlank
  @Pattern(regexp = Patterns.PASSWORD)
  @Schema(description = "Password for the new user, must meet security requirements", example = "P@ssw0rd123", requiredMode = RequiredMode.REQUIRED)
  private String password;

  @NotBlank
  @Email
  @Schema(description = "Email address of the new user", example = "john.doe@example.com", requiredMode = RequiredMode.REQUIRED)
  private String email;

  @NotBlank
  @Schema(description = "Phone number of the new user", example = "+1234567890", requiredMode = RequiredMode.REQUIRED)
  private String phoneNumber;

  @NotNull
  @Schema(description = "The role of user", example = "USER", requiredMode = RequiredMode.REQUIRED)
  private Role role;
}
