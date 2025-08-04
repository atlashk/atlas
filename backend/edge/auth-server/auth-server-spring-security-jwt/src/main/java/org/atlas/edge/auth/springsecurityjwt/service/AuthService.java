package org.atlas.edge.auth.springsecurityjwt.service;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.SessionEntity;
import org.atlas.domain.auth.entity.UserEntity;
import org.atlas.domain.auth.repository.SessionRepository;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.edge.auth.springsecurityjwt.model.GenerateOneTimeTokenRequest;
import org.atlas.edge.auth.springsecurityjwt.model.GenerateOneTimeTokenResponse;
import org.atlas.edge.auth.springsecurityjwt.model.LoginRequest;
import org.atlas.edge.auth.springsecurityjwt.model.LoginResponse;
import org.atlas.edge.auth.springsecurityjwt.model.OneTimeTokenLoginRequest;
import org.atlas.edge.auth.springsecurityjwt.model.RefreshTokenRequest;
import org.atlas.edge.auth.springsecurityjwt.model.RefreshTokenResponse;
import org.atlas.edge.auth.springsecurityjwt.security.UserDetailsImpl;
import org.atlas.framework.constant.SecurityConstant;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.error.AppError;
import org.atlas.framework.jwt.Jwt;
import org.atlas.framework.util.UUIDGenerator;
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
  private final SessionRepository sessionRepository;
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final OneTimeTokenService oneTimeTokenService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Transactional
  public LoginResponse login(LoginRequest request) throws IOException, InvalidKeySpecException {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(request.getIdentifier(), request.getPassword());
    return doLogin(authentication);
  }

  @Transactional
  public LoginResponse oneTimeTokenLogin(OneTimeTokenLoginRequest request)
      throws IOException, InvalidKeySpecException {
    OneTimeTokenAuthenticationToken authentication =
        new OneTimeTokenAuthenticationToken(request.getIdentifier(), request.getToken());
    return doLogin(authentication);
  }

  public GenerateOneTimeTokenResponse generateOneTimeToken(GenerateOneTimeTokenRequest request) {
    OneTimeToken token = oneTimeTokenService.generate(
        new org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest(
            request.getIdentifier()));
    return new GenerateOneTimeTokenResponse(token.getTokenValue());
  }

  @Transactional
  public RefreshTokenResponse refreshToken(RefreshTokenRequest request)
      throws IOException, InvalidKeySpecException {
    // Require deviceId
    String deviceId = Contexts.getDeviceId();
    if (deviceId == null) {
      throw new DomainException(AppError.BAD_REQUEST, "Device ID is required");
    }

    // Parse refresh token
    Jwt refreshTokenJwt;
    try {
      refreshTokenJwt = tokenService.parseToken(request.getRefreshToken());
    } catch (Exception e) {
      throw new DomainException(AppError.UNAUTHORIZED, "Invalid refresh token");
    }

    // Retrieve session from database and validate request
    SessionEntity sessionEntity = sessionRepository.findById(refreshTokenJwt.getSessionId())
        .orElseThrow(() -> new DomainException(AppError.UNAUTHORIZED, "Session not found"));
    if (!sessionEntity.getRefreshToken().equals(request.getRefreshToken())) {
      throw new DomainException(AppError.UNAUTHORIZED, "Invalid refresh token");
    }
    if (!sessionEntity.getDeviceId().equals(deviceId)) {
      throw new DomainException(AppError.UNAUTHORIZED, "Invalid device ID");
    }

    // Reissue tokens
    UserEntity userEntity = userRepository.findById(sessionEntity.getUserId())
        .orElseThrow(() -> new DomainException(AppError.USER_NOT_FOUND));
    UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

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

    // Update refresh token in database
    sessionEntity.setRefreshToken(refreshToken);
    sessionRepository.update(sessionEntity);

    return new RefreshTokenResponse(accessToken, refreshToken);
  }

  @Transactional
  public void logout() {
    ContextInfo contextInfo = Contexts.get();

    // Remove session from database
    sessionRepository.deleteById(contextInfo.getSessionId());

    // Revoke session
    revokeSession(contextInfo.getSessionId(), contextInfo.getExpiresAt());

    // Update last logout timestamp in Redis
    updateLastLogoutTs(contextInfo.getUserId());
  }

  public void forceLogoutOnAllDevices() {
    Integer userId = Contexts.getUserId();
    updateLastLogoutTs(userId);
  }

  private LoginResponse doLogin(Authentication authenticationRequest)
      throws IOException, InvalidKeySpecException {
    // Require deviceId
    String deviceId = Contexts.getDeviceId();
    if (deviceId == null) {
      throw new DomainException(AppError.BAD_REQUEST, "X-Device-Id is required");
    }

    Authentication authentication = authenticationManager.authenticate(authenticationRequest);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    // Issue refresh token
    Date now = new Date();
    Date refreshTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.REFRESH_TOKEN_EXPIRATION_TIME * 1000);
    String refreshToken = tokenService.issueRefreshToken(userDetails, now, refreshTokenExpiresAt);

    // Create new session
    SessionEntity sessionEntity = new SessionEntity();
    sessionEntity.setId(UUIDGenerator.generate());
    sessionEntity.setUserId(userDetails.getUserId());
    sessionEntity.setDeviceId(deviceId);
    sessionEntity.setRefreshToken(refreshToken);
    sessionRepository.insert(sessionEntity);

    userDetails.setSessionId(sessionEntity.getId());

    // Issue access token
    now = new Date();
    Date accessTokenExpiresAt = new Date(
        now.getTime() + SecurityConstant.ACCESS_TOKEN_EXPIRATION_TIME * 1000);
    String accessToken = tokenService.issueAccessToken(userDetails, now, accessTokenExpiresAt);

    return new LoginResponse(accessToken, refreshToken);
  }

  private void revokeSession(String sessionId, Date expiresAt) {
    String redisKey = String.format(SecurityConstant.SESSION_BLACKLISTED_REDIS_KEY_FORMAT,
        sessionId);
    long remainingMillis = expiresAt.getTime() - System.currentTimeMillis();
    if (remainingMillis > 0) {
      redisTemplate.opsForValue().set(redisKey, true, Duration.ofMillis(remainingMillis));
    }
  }

  private void updateLastLogoutTs(Integer userId) {
    long lastLogoutTs = new Date().getTime();
    String redisKey = String.format("user:%d:lastLogoutTs", userId);
    redisTemplate.opsForValue().set(redisKey, lastLogoutTs);
  }
}
