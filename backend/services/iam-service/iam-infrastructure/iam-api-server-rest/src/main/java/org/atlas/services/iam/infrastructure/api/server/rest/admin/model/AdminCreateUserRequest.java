package org.atlas.services.iam.infrastructure.api.server.rest.admin.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.constant.Patterns;
import org.atlas.libs.framework.domain.user.UserRole;

@Schema(description = "Request object for creating a user containing required data")
@Getter
@Setter
public class AdminCreateUserRequest {

  @NotBlank
  @Schema(description = "Username for the new user", example = "john_doe", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotBlank
  @Pattern(regexp = Patterns.PASSWORD)
  @Schema(description = "Password for the new user, must meet security requirements", example = "P@ssw0rd123", requiredMode = RequiredMode.REQUIRED)
  private String password;

  @NotBlank
  @Schema(description = "First name of the new user", example = "John", requiredMode = RequiredMode.REQUIRED)
  private String firstName;

  @NotBlank
  @Schema(description = "Last name of the new user", example = "Doe", requiredMode = RequiredMode.REQUIRED)
  private String lastName;

  @NotBlank
  @Email
  @Schema(description = "Email address of the new user", example = "john.doe@example.com", requiredMode = RequiredMode.REQUIRED)
  private String email;

  @NotBlank
  @Schema(description = "Phone number of the new user", example = "+1234567890", requiredMode = RequiredMode.REQUIRED)
  private String phoneNumber;

  @Schema(description = "Role of the new user", example = "USER")
  private UserRole role;
}
