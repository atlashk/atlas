package org.atlas.edge.auth.springsecurityjwt.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "Request object for refreshing the access token using a refresh token.")
public class RefreshTokenRequest {

  @NotBlank
  @Schema(description = "The refresh token used to obtain a new access token.", example = "dGhpc0lzUmVmcmVzaFRva2Vu")
  private String refreshToken;
}
