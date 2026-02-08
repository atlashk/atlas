package org.atlas.services.iam.application.keycloak.auth;

import com.auth0.jwt.JWT;
import java.time.Duration;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.StringUtil;
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
  private final KvStoreService kvStoreService;

  @Override
  public Map<String, Object> jwkSet() {
    throw new UnsupportedOperationException();
  }

  @Override
  public LoginOutput login(LoginInput input) {
    TokenResponse tokenResponse = keycloakAuthenticationClient.login(input.getUsername(),
        input.getPassword());
    return new LoginOutput(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
  }

  @Override
  public RefreshTokenOutput refreshToken(RefreshTokenInput input) {
    TokenResponse tokenResponse = keycloakAuthenticationClient.refreshToken(input.getRefreshToken());
    return new RefreshTokenOutput(tokenResponse.getAccessToken(), tokenResponse.getRefreshToken());
  }

  @Override
  public void logout(String accessToken) {
    if (StringUtil.isBlank(accessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Missing access token");
    }

    String hashedAccessToken = HashingUtil.sha256ToHex(accessToken);
    if (kvStoreService.exists(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME,
        hashedAccessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED,
          "Access token has been already inactivated");
    }

    Date expiresAt;
    try {
      expiresAt = JWT.decode(accessToken).getExpiresAt();
    } catch (Exception e) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Invalid access token");
    }

    long now = DateUtil.timestamp();
    long ttlMs;
    if (expiresAt == null) {
      ttlMs = Duration.ofDays(1).toMillis();
    } else {
      ttlMs = Math.max(1000L, expiresAt.getTime() - now);
    }
    kvStoreService.put(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME, hashedAccessToken, "1",
        Duration.ofMillis(ttlMs));
  }

  @Override
  public LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) {
    throw new DomainException(DomainError.BAD_REQUEST, "Not supported");
  }

  @Override
  public GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input) {
    throw new DomainException(DomainError.BAD_REQUEST, "Not supported");
  }
}
