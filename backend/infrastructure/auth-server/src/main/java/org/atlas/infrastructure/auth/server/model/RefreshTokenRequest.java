package org.atlas.infrastructure.auth.server.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Request object for refreshing the access token using a refresh token.")
public class RefreshTokenRequest {

  @NotBlank
  @Schema(description = "The refresh token used to obtain a new access token.", example = "dGhpc0lzUmVmcmVzaFRva2Vu")
  private String refreshToken;
}
