package org.atlas.infrastructure.auth.server.service;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.jwt.Jwt;
import org.atlas.framework.security.SecurityConstant;
import org.atlas.infrastructure.auth.server.model.GenerateOneTimeTokenRequest;
import org.atlas.infrastructure.auth.server.model.GenerateOneTimeTokenResponse;
import org.atlas.infrastructure.auth.server.model.LoginRequest;
import org.atlas.infrastructure.auth.server.model.LoginResponse;
import org.atlas.infrastructure.auth.server.model.OneTimeTokenLoginRequest;
import org.atlas.infrastructure.auth.server.model.RefreshTokenRequest;
import org.atlas.infrastructure.auth.server.model.RefreshTokenResponse;
import org.atlas.infrastructure.auth.server.security.UserDetailsImpl;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final OneTimeTokenService oneTimeTokenService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Transactional
  public LoginResponse login(LoginRequest request) throws IOException, InvalidKeySpecException {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
    return doLogin(authentication);
  }

  @Transactional
  public LoginResponse oneTimeTokenLogin(OneTimeTokenLoginRequest request)
      throws IOException, InvalidKeySpecException {
    OneTimeTokenAuthenticationToken authentication =
        new OneTimeTokenAuthenticationToken(request.getUsername(), request.getToken());
    return doLogin(authentication);
  }

  public GenerateOneTimeTokenResponse generateOneTimeToken(GenerateOneTimeTokenRequest request) {
    OneTimeToken token = oneTimeTokenService.generate(
        new org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest(
            request.getUsername()));
    return new GenerateOneTimeTokenResponse(token.getTokenValue());
  }

  @Transactional
  public RefreshTokenResponse refreshToken(RefreshTokenRequest request)
      throws IOException, InvalidKeySpecException {
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

  @Transactional
  public void logout() {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo == null) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Unauthorized");
    }

    // Update last logout timestamp in Redis
    updateLastLogoutTs(contextInfo.getUserId());
  }

  private LoginResponse doLogin(Authentication authenticationRequest)
      throws IOException, InvalidKeySpecException {
    Authentication authentication = authenticationManager.authenticate(authenticationRequest);
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

  private void updateLastLogoutTs(Integer userId) {
    long lastLogoutTs = new Date().getTime();
    String redisKey = String.format("user:%d:lastLogoutTs", userId);
    redisTemplate.opsForValue().set(redisKey, lastLogoutTs);
  }
}
