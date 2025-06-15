package org.atlas.edge.auth.springsecurityjwt.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response object for user login containing access and refresh tokens.")
public class LoginResponse {

  @Schema(description = "Access token for authenticated user.", example = "eyJhbGciOiJIUzI1NiIsInR...")
  private String accessToken;

  @Schema(description = "Refresh token to obtain new access tokens.", example = "eyJhbGciOiJIUzI1NiIsInR...")
  private String refreshToken;
}
