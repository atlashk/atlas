package org.atlas.infrastructure.api.server.rest.impl.auth.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request object for user generate one-time token")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class GenerateOneTimeTokenRequest {

  @NotBlank
  @Schema(description = "Username, email, of phone number of the user attempting to log in", example = "john_doe", requiredMode = RequiredMode.REQUIRED)
  private String username;
}
