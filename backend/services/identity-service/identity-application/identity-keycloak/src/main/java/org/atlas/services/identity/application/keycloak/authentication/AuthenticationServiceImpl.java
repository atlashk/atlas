package org.atlas.services.identity.application.keycloak.authentication;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakAuthenticationClient;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.core.model.TokenResponse;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.domain.error.DomainError;
import org.atlas.services.identity.domain.exception.DomainException;
import org.atlas.services.identity.port.in.authentication.model.ChangePasswordInput;
import org.atlas.services.identity.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.services.identity.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.services.identity.port.in.authentication.model.LoginInput;
import org.atlas.services.identity.port.in.authentication.model.LoginOutput;
import org.atlas.services.identity.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.services.identity.port.in.authentication.model.RefreshTokenInput;
import org.atlas.services.identity.port.in.authentication.model.RefreshTokenOutput;
import org.atlas.services.identity.port.in.authentication.service.AuthenticationService;
import org.atlas.services.identity.port.out.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;
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
  @Transactional(readOnly = true)
  public void changePassword(ChangePasswordInput input) {
    String userId = Contexts.getUserId();
    UserEntity user = userRepository.findById(userId)
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
  public LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception {
    throw new DomainException(CommonDomainError.BAD_REQUEST, "Not supported");
  }

  @Override
  public GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input) {
    throw new DomainException(CommonDomainError.BAD_REQUEST, "Not supported");
  }
}
