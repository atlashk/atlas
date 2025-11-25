package org.atlas.edge.auth.springsecurityjwt.service;

import java.io.IOException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.User;
import org.atlas.domain.auth.model.GenerateOneTimeTokenRequest;
import org.atlas.domain.auth.model.GenerateOneTimeTokenResponse;
import org.atlas.domain.auth.model.LoginRequest;
import org.atlas.domain.auth.model.LoginResponse;
import org.atlas.domain.auth.model.OneTimeTokenLoginRequest;
import org.atlas.domain.auth.model.RefreshTokenRequest;
import org.atlas.domain.auth.model.RefreshTokenResponse;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.domain.auth.service.AuthService;
import org.atlas.edge.auth.springsecurityjwt.security.UserDetailsImpl;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.jwt.Jwt;
import org.atlas.framework.security.SecurityConstant;
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
public class AuthServiceImpl implements AuthService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final TokenService tokenService;
  private final OneTimeTokenService oneTimeTokenService;
  private final RedisTemplate<String, Object> redisTemplate;

  @Transactional
  @Override
  public LoginResponse login(LoginRequest request) throws Exception {
    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword());
    return doLogin(authentication);
  }

  @Transactional
  @Override
  public LoginResponse oneTimeTokenLogin(OneTimeTokenLoginRequest request) throws Exception {
    OneTimeTokenAuthenticationToken authentication =
        new OneTimeTokenAuthenticationToken(request.getUsername(), request.getToken());
    return doLogin(authentication);
  }

  @Override
  public GenerateOneTimeTokenResponse generateOneTimeToken(GenerateOneTimeTokenRequest request) {
    OneTimeToken token = oneTimeTokenService.generate(
        new org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest(
            request.getUsername()));
    return new GenerateOneTimeTokenResponse(token.getTokenValue());
  }

  @Transactional
  @Override
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

  @Transactional
  @Override
  public void logout() {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo == null) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Unauthorized");
    }

    // Update last logout timestamp in Redis
    long lastLogoutTs = new Date().getTime();
    String redisKey = String.format("user:%d:lastLogoutTs", contextInfo.getUserId());
    redisTemplate.opsForValue().set(redisKey, lastLogoutTs);
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
}
