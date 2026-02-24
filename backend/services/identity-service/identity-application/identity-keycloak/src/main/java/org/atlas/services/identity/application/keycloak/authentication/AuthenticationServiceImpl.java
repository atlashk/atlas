package org.atlas.services.identity.application.keycloak.authentication;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.error.DomainError;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.random.RandomUtil;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakAuthenticationClient;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.core.model.TokenResponse;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.services.identity.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.services.identity.port.in.authentication.model.LoginInput;
import org.atlas.services.identity.port.in.authentication.model.LoginOutput;
import org.atlas.services.identity.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.services.identity.port.in.authentication.model.RefreshTokenInput;
import org.atlas.services.identity.port.in.authentication.model.RefreshTokenOutput;
import org.atlas.services.identity.port.in.authentication.service.AuthenticationService;
import org.atlas.services.identity.port.in.authentication.model.ChangePasswordInput;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

  private final KeycloakAuthenticationClient keycloakAuthenticationClient;
  private final KeycloakUserClient keycloakUserClient;

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
  public void changePassword(ChangePasswordInput input) {
    String userId = Contexts.getUserId();
    UserEntity user = keycloakUserClient.retrieveUser(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    // Verify the current password
    try {
      keycloakAuthenticationClient.login(user.getUsername(), input.getOldPassword());
    } catch (Exception e) {
      throw new DomainException(DomainError.WRONG_PASSWORD);
    }

    keycloakAuthenticationClient.changePassword(userId, input.getNewPassword());
  }

  @Override
  public String resetPassword(String userId) {
    keycloakUserClient.retrieveUser(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    String newPassword = RandomUtil.randomPassword(12, true, true, true);
    keycloakAuthenticationClient.changePassword(userId, newPassword);
    return newPassword;
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
