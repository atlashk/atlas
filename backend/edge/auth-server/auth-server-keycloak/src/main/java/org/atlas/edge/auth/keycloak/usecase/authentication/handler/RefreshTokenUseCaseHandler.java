package org.atlas.edge.auth.keycloak.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.RefreshTokenUseCase;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenInput;
import org.atlas.domain.auth.usecase.authentication.model.RefreshTokenOutput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.infrastructure.auth.keycloak.client.KeycloakClient;
import org.atlas.infrastructure.auth.keycloak.model.TokenResponse;

@UseCaseHandler
@RequiredArgsConstructor
public class RefreshTokenUseCaseHandler implements RefreshTokenUseCase {
  private final KeycloakClient keycloakClient;

  public RefreshTokenOutput handle(RefreshTokenInput input) throws Exception {
    TokenResponse res = keycloakClient.refreshToken(input.getRefreshToken());
    return new RefreshTokenOutput(res.getAccessToken(), res.getRefreshToken());
  }
}
