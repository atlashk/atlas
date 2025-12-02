package org.atlas.edge.auth.keycloak.api.authentication.service;

import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.keycloak.api.authentication.model.LoginRequest;
import org.atlas.edge.auth.keycloak.api.authentication.model.LoginResponse;
import org.atlas.edge.auth.keycloak.api.authentication.model.RefreshTokenRequest;
import org.atlas.edge.auth.keycloak.api.authentication.model.RefreshTokenResponse;
import org.atlas.infrastructure.auth.keycloak.client.KeycloakClient;
import org.atlas.infrastructure.auth.keycloak.model.TokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final KeycloakClient keycloakClient;

  public LoginResponse login(LoginRequest request) throws Exception {
    TokenResponse res = keycloakClient.login(request.getUsername(), request.getPassword());
    return new LoginResponse(res.getAccessToken(), res.getRefreshToken());
  }

  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
    TokenResponse res = keycloakClient.refreshToken(request.getRefreshToken());
    return new RefreshTokenResponse(res.getAccessToken(), res.getRefreshToken());
  }

  public void logout(String accessToken) throws Exception {
    keycloakClient.revokeAccessToken(accessToken);
  }
}
