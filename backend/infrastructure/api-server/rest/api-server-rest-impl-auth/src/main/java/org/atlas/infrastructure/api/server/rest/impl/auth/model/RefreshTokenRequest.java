package org.atlas.infrastructure.api.server.rest.impl.auth.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request object for refreshing the access token using a refresh token")
@Getter
@Setter
public class RefreshTokenRequest {

  @NotBlank
  @Schema(description = "The refresh token used to obtain a new access token", example = "dGhpc0lzUmVmcmVzaFRva2Vu", requiredMode = RequiredMode.REQUIRED)
  private String refreshToken;
}
