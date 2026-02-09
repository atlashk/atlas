package org.atlas.services.iam.application.keycloak.auth;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakAuthenticationClient;
import org.atlas.services.iam.application.keycloak.core.model.TokenResponse;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenInput;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenOutput;
import org.atlas.services.iam.port.in.auth.model.LoginInput;
import org.atlas.services.iam.port.in.auth.model.LoginOutput;
import org.atlas.services.iam.port.in.auth.model.OneTimeTokenLoginInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenOutput;
import org.atlas.services.iam.port.in.auth.service.AuthenticationService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  private final KeycloakAuthenticationClient keycloakAuthenticationClient;

  @Override
  public Map<String, Object> jwkSet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LoginOutput login(LoginInput input) throws Exception {
    TokenResponse tokenResponse = keycloakAuthenticationClient.login(input.getUsername(),
        input.getPassword());
    return new LoginOutput(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
  }

  @Override
  public RefreshTokenOutput refreshToken(RefreshTokenInput input) throws Exception {
    TokenResponse tokenResponse = keycloakAuthenticationClient.refreshToken(
        input.getRefreshToken());
    return new RefreshTokenOutput(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
  }

  @Override
  public void logout(String accessToken) throws Exception {
    keycloakAuthenticationClient.logout(accessToken);
  }

  @Override
  public LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception {
    throw new DomainException(DomainError.BAD_REQUEST, "Not supported");
  }

  @Override
  public GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input) {
    throw new DomainException(DomainError.BAD_REQUEST, "Not supported");
  }
}
