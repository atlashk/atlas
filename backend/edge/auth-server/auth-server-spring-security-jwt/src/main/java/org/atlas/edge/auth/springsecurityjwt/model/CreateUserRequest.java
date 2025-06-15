package org.atlas.edge.auth.springsecurityjwt.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.atlas.framework.constant.Patterns;

@Data
@Schema(description = "Request object for user registration containing required data.")
public class CreateUserRequest {

  @NotBlank
  @Schema(description = "Username for the new user.", example = "john_doe")
  private String username;

  @NotBlank
  @Pattern(regexp = Patterns.PASSWORD)
  @Schema(description = "Password for the new user, must meet security requirements.", example = "P@ssw0rd123")
  private String password;

  @NotBlank
  @Email
  @Schema(description = "Email address of the new user.", example = "john.doe@example.com")
  private String email;

  @NotBlank
  @Schema(description = "Phone number of the new user.", example = "+1234567890")
  private String phoneNumber;
}
