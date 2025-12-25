package org.atlas.edge.auth.keycloak.api.authentication.service;

import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.keycloak.api.authentication.model.LoginRequest;
import org.atlas.edge.auth.keycloak.api.authentication.model.LoginResponse;
import org.atlas.edge.auth.keycloak.api.authentication.model.RefreshTokenRequest;
import org.atlas.edge.auth.keycloak.api.authentication.model.RefreshTokenResponse;
import org.atlas.infrastructure.iam.keycloak.client.KeycloakAuthenticationClient;
import org.atlas.infrastructure.iam.keycloak.model.TokenResponse;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final KeycloakAuthenticationClient keycloakAuthenticationClient;

  public LoginResponse login(LoginRequest request) {
    TokenResponse keycloakResponse = keycloakAuthenticationClient.login(
        request.getUsername(), request.getPassword());
    return new LoginResponse(keycloakResponse.getAccessToken(), keycloakResponse.getRefreshToken());
  }

  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) {
    TokenResponse keycloakResponse = keycloakAuthenticationClient.refreshToken(
        request.getRefreshToken());
    return new RefreshTokenResponse(keycloakResponse.getAccessToken(),
        keycloakResponse.getRefreshToken());
  }

  public void logout(String accessToken) {
    keycloakAuthenticationClient.revokeAccessToken(accessToken);
  }
}
