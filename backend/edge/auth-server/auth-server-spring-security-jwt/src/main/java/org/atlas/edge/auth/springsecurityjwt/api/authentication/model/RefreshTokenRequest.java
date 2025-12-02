package org.atlas.edge.auth.springsecurityjwt.api.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Request object for refreshing the access token using a refresh token")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RefreshTokenRequest {

  @NotBlank
  @Schema(description = "The refresh token used to obtain a new access token", example = "dGhpc0lzUmVmcmVzaFRva2Vu", requiredMode = RequiredMode.REQUIRED)
  private String refreshToken;
}
