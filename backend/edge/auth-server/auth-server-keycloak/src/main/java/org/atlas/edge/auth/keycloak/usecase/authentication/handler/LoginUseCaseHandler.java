package org.atlas.edge.auth.keycloak.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.LoginUseCase;
import org.atlas.domain.auth.usecase.authentication.model.LoginInput;
import org.atlas.domain.auth.usecase.authentication.model.LoginOutput;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.infrastructure.auth.keycloak.client.KeycloakClient;
import org.atlas.infrastructure.auth.keycloak.model.TokenResponse;

@UseCaseHandler
@RequiredArgsConstructor
public class LoginUseCaseHandler implements LoginUseCase {
  private final KeycloakClient keycloakClient;

  @Override
  public LoginOutput handle(LoginInput input) throws Exception {
    TokenResponse res = keycloakClient.login(input.getUsername(), input.getPassword());
    return new LoginOutput(res.getAccessToken(), res.getRefreshToken());
  }
}
