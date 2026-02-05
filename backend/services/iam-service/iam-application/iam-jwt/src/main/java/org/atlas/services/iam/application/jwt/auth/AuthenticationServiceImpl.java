package org.atlas.services.iam.application.jwt.auth;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.jwt.Jwt;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.application.jwt.core.TokenService;
import org.atlas.services.iam.application.jwt.core.UserDetailsImpl;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenInput;
import org.atlas.services.iam.port.in.auth.model.GenerateOneTimeTokenOutput;
import org.atlas.services.iam.port.in.auth.model.LoginInput;
import org.atlas.services.iam.port.in.auth.model.LoginOutput;
import org.atlas.services.iam.port.in.auth.model.OneTimeTokenLoginInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenInput;
import org.atlas.services.iam.port.in.auth.model.RefreshTokenOutput;
import org.atlas.services.iam.port.in.auth.service.AuthenticationService;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {

  private final AuthenticationManager authenticationManager;
  private final OneTimeTokenService oneTimeTokenService;
  private final UserRepository userRepository;
  private final TokenService tokenService;
  private final KvStoreService kvStoreService;

  @Override
  public LoginOutput login(LoginInput input) throws Exception {
    Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
        input.getUsername(), input.getPassword());
    return doLogin(authenticationToken);
  }

  @Override
  @Transactional(readOnly = true)
  public RefreshTokenOutput refreshToken(RefreshTokenInput input) throws Exception {
    // Parse refresh token
    Jwt refreshTokenJwt;
    try {
      refreshTokenJwt = tokenService.parseToken(input.getRefreshToken());
    } catch (Exception e) {
      throw new DomainException(DomainError.UNAUTHORIZED, "Invalid refresh token");
    }

    // Reissue tokens
    UserEntity user = userRepository.findById(refreshTokenJwt.getUserId())
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

    return new RefreshTokenOutput(accessToken, refreshToken);
  }

  @Override
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

  @Override
  public LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception {
    Authentication authenticationToken = new OneTimeTokenAuthenticationToken(
        input.getUsername(), input.getToken());
    return doLogin(authenticationToken);
  }

  @Override
  public GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input) {
    OneTimeToken token = oneTimeTokenService.generate(
        new GenerateOneTimeTokenRequest(input.getUsername()));
    return new GenerateOneTimeTokenOutput(token.getTokenValue());
  }

  private LoginOutput doLogin(Authentication authenticationToken) throws Exception {
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

    return new LoginOutput(accessToken, refreshToken);
  }
}
