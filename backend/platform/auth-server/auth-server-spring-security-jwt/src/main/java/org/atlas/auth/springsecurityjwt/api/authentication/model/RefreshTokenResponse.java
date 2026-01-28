package org.atlas.auth.springsecurityjwt.api.authentication.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Response object for refreshing token containing access and refresh tokens")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefreshTokenResponse {

  @Schema(description = "Access token for authenticated user", example = "eyJhbGciOiJIUzI1NiIsInR...")
  private String accessToken;

  @Schema(description = "Refresh token to obtain new access tokens", example = "eyJhbGciOiJIUzI1NiIsInR...")
  private String refreshToken;
}
