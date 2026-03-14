package org.atlas.services.identity.api.rest.authentication.model;

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
public class OneTimeTokenLoginRequest {

  @NotBlank
  @Schema(description = "Email of the user attempting one-time-token login", example = "john.doe@example.com", requiredMode = RequiredMode.REQUIRED)
  private String email;

  @NotBlank
  @Schema(description = "The provided one-time token", example = "123456", requiredMode = RequiredMode.REQUIRED)
  private String token;
}
