package org.atlas.auth.springsecurityjwt.api.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request object for user login")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class LoginRequest {

  @NotBlank
  @Schema(description = "Username, email, or phone number of the user attempting to log in", example = "john_doe", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotBlank
  @Schema(description = "Password of the user attempting to log in", example = "P@ssw0rd", requiredMode = RequiredMode.REQUIRED)
  private String password;
}
