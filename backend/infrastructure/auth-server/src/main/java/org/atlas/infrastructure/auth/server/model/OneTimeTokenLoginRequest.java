package org.atlas.infrastructure.auth.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Request object for user login.")
public class OneTimeTokenLoginRequest {

  @NotBlank
  @Schema(description = "Username, email, or phone number of the user attempting to log in.", example = "john_doe")
  private String username;

  @NotBlank
  @Schema(description = "The provided one-time token.", example = "123456")
  private String token;
}
