package org.atlas.infrastructure.auth.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request object for user login")
@Getter
@Setter
public class OneTimeTokenLoginRequest {

  @NotBlank
  @Schema(description = "Username, email, or phone number of the user attempting to log in", example = "john_doe", requiredMode = RequiredMode.REQUIRED)
  private String username;

  @NotBlank
  @Schema(description = "The provided one-time token", example = "123456", requiredMode = RequiredMode.REQUIRED)
  private String token;
}
