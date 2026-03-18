package org.atlas.platform.authorization.core;

import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.security.JwtUtil;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.domain.entity.UserEntity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;

@RequiredArgsConstructor
public class PublicClientRefreshTokenGenerator implements OAuth2TokenGenerator<OAuth2RefreshToken> {

  /**
   * We do not use the built-in OAuth2RefreshTokenGenerator because:
   * 1) public clients (PKCE + client_authentication_method=none) still need refresh tokens,
   * 2) refresh tokens must be JWT-formatted to stay compatible with the
   *    /api/authentication/refresh-token flow, which verifies/parses tokens via JwtUtil.
   */
  private final UserRepository userRepository;

  @Override
  public OAuth2RefreshToken generate(OAuth2TokenContext context) {
    if (!OAuth2TokenType.REFRESH_TOKEN.equals(context.getTokenType())) {
      return null;
    }
    if (!AuthorizationGrantType.AUTHORIZATION_CODE.equals(context.getAuthorizationGrantType())
        && !AuthorizationGrantType.REFRESH_TOKEN.equals(context.getAuthorizationGrantType())) {
      return null;
    }
    if (!context.getRegisteredClient().getAuthorizationGrantTypes()
        .contains(AuthorizationGrantType.REFRESH_TOKEN)) {
      return null;
    }
    OAuth2Authorization authorization = context.getAuthorization();
    if (authorization == null) {
      return null;
    }
    String principalName = authorization.getPrincipalName();
    if (StringUtil.isBlank(principalName)) {
      return null;
    }
    Optional<UserEntity> optionalUser = userRepository.findByEmail(principalName);
    if (optionalUser.isEmpty()) {
      optionalUser = userRepository.findById(principalName);
    }
    if (optionalUser.isEmpty()) {
      return null;
    }
    UserEntity user = optionalUser.get();
    Principal principal = user.toPrincipal();
    Instant issuedAt = Instant.now();
    Instant expiresAt =
        issuedAt.plus(context.getRegisteredClient().getTokenSettings().getRefreshTokenTimeToLive());
    String refreshTokenValue;
    try {
      refreshTokenValue = JwtUtil.issueRefreshToken(principal);
    } catch (Exception exception) {
      throw new IllegalStateException("Cannot issue OAuth2 refresh token", exception);
    }
    return new OAuth2RefreshToken(refreshTokenValue, issuedAt, expiresAt);
  }
}
