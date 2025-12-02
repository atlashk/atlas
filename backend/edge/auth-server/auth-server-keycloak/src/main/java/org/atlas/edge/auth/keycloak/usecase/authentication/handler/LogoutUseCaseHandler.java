package org.atlas.edge.auth.keycloak.usecase.authentication.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.usecase.authentication.handler.LogoutUseCase;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.StringUtil;
import org.atlas.infrastructure.auth.keycloak.client.KeycloakClient;

@UseCaseHandler
@RequiredArgsConstructor
public class LogoutUseCaseHandler implements LogoutUseCase {
  private final KeycloakClient keycloakClient;

  @Override
  public void handle(String accessToken) throws Exception {
    if (StringUtil.isBlank(accessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Missing access token");
    }
    keycloakClient.revokeAccessToken(accessToken);
  }
}
