package org.atlas.edge.auth.keycloak.api.authentication.service;

import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.keycloak.api.authentication.model.LoginRequest;
import org.atlas.edge.auth.keycloak.api.authentication.model.LoginResponse;
import org.atlas.edge.auth.keycloak.api.authentication.model.RefreshTokenRequest;
import org.atlas.edge.auth.keycloak.api.authentication.model.RefreshTokenResponse;
import org.atlas.infrastructure.auth.keycloak.client.AuthenticationClient;
import org.atlas.infrastructure.auth.keycloak.model.TokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final AuthenticationClient authenticationClient;

  public LoginResponse login(LoginRequest request) throws Exception {
    TokenResponse keycloakResponse = authenticationClient.login(
        request.getUsername(), request.getPassword());
    return new LoginResponse(keycloakResponse.getAccessToken(), keycloakResponse.getRefreshToken());
  }

  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
    TokenResponse keycloakResponse = authenticationClient.refreshToken(
        request.getRefreshToken());
    return new RefreshTokenResponse(keycloakResponse.getAccessToken(),
        keycloakResponse.getRefreshToken());
  }

  public void logout(String accessToken) throws Exception {
    authenticationClient.revokeAccessToken(accessToken);
  }
}
