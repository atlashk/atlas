package org.atlas.auth.springsecurityjwt.api.authentication.service;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.atlas.auth.common.domain.entity.User;
import org.atlas.auth.common.domain.repository.UserRepository;
import org.atlas.auth.springsecurityjwt.api.authentication.model.LoginRequest;
import org.atlas.auth.springsecurityjwt.api.authentication.model.LoginResponse;
import org.atlas.auth.springsecurityjwt.api.authentication.model.OneTimeTokenLoginRequest;
import org.atlas.auth.springsecurityjwt.api.authentication.model.RefreshTokenRequest;
import org.atlas.auth.springsecurityjwt.api.authentication.model.RefreshTokenResponse;
import org.atlas.auth.springsecurityjwt.security.TokenService;
import org.atlas.auth.springsecurityjwt.security.UserDetailsImpl;
import org.atlas.common.framework.cryptography.HashingUtil;
import org.atlas.common.framework.domain.common.error.DomainError;
import org.atlas.common.framework.domain.common.exception.DomainException;
import org.atlas.common.framework.jwt.Jwt;
import org.atlas.common.framework.kvstore.KvStoreService;
import org.atlas.common.framework.security.SecurityConstant;
import org.atlas.common.framework.util.DateUtil;
import org.atlas.common.framework.util.StringUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final KvStoreService kvStoreService;

  public LoginResponse login(LoginRequest request) throws Exception {
    Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
        request.getUsername(), request.getPassword());
    return doLogin(authenticationToken);
  }

  public LoginResponse oneTimeTokenLogin(OneTimeTokenLoginRequest request) throws Exception {
    Authentication authenticationToken = new OneTimeTokenAuthenticationToken(
        request.getUsername(), request.getToken());
    return doLogin(authenticationToken);
  }

  private LoginResponse doLogin(Authentication authenticationToken) throws Exception {
    Authentication authentication = authenticationManager.authenticate(authenticationToken);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    // Issue refresh token
    Date now = new Date();
    Date refreshTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME * 1000);
    String refreshToken = tokenService.issueRefreshToken(userDetails, now, refreshTokenExpiresAt);

    // Issue access token
    now = new Date();
    Date accessTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME * 1000);
    String accessToken = tokenService.issueAccessToken(userDetails, now, accessTokenExpiresAt);

    return new LoginResponse(accessToken, refreshToken);
  }

  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
    // Parse refresh token
    Jwt refreshTokenJwt;
    try {
      refreshTokenJwt = tokenService.parseToken(request.getRefreshToken());
    } catch (Exception e) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Invalid refresh token");
    }

    // Reissue tokens
    User user = userRepository.findById(refreshTokenJwt.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    UserDetailsImpl userDetails = new UserDetailsImpl(user);

    // Issue new access token
    Date now = new Date();
    Date accessTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME * 1000);
    String accessToken = tokenService.issueAccessToken(userDetails, now, accessTokenExpiresAt);

    // Issue new refresh token
    now = new Date();
    Date refreshTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME * 1000);
    String refreshToken = tokenService.issueRefreshToken(userDetails, now, refreshTokenExpiresAt);

    return new RefreshTokenResponse(accessToken, refreshToken);
  }

  public void logout(String accessToken) throws Exception {
    if (StringUtil.isBlank(accessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Missing access token");
    }

    String hashedAccessToken = HashingUtil.sha256ToHex(accessToken);
    if (kvStoreService.exists(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME,
        hashedAccessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED,
          "Access token has been already inactivated");
    }

    Jwt jwt = tokenService.parseToken(accessToken);
    long now = DateUtil.timestamp();
    long ttlMs = Math.max(1000L, jwt.getExpiresAt().getTime() - now);
    kvStoreService.put(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME, hashedAccessToken, "1",
        Duration.ofMillis(ttlMs));
  }
}
