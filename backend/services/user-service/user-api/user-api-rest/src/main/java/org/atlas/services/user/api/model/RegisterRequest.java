package org.atlas.services.user.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.constant.Patterns;

@Schema(description = "Request object for user registration containing required data")
@Getter
@Setter
public class RegisterRequest {

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

  @Schema(description = "Phone number of the new user", example = "+1234567890")
  private String phoneNumber;

  @NotBlank
  @Pattern(regexp = Patterns.PASSWORD)
  @Schema(description = "Password for the new user, must meet security requirements", example = "P@ssw0rd123", requiredMode = RequiredMode.REQUIRED)
  private String password;
}
