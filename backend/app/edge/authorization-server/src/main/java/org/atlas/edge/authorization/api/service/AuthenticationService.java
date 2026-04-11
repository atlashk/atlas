package org.atlas.edge.authorization.api.service;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.security.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.security.jwt.JwtUtil;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.LegacyDateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.edge.authorization.api.model.ChangePasswordRequest;
import org.atlas.edge.authorization.api.model.GenerateOneTimeTokenRequest;
import org.atlas.edge.authorization.api.model.GenerateOneTimeTokenResponse;
import org.atlas.edge.authorization.api.model.LoginRequest;
import org.atlas.edge.authorization.api.model.LoginResponse;
import org.atlas.edge.authorization.api.model.OneTimeTokenLoginRequest;
import org.atlas.edge.authorization.api.model.RefreshTokenRequest;
import org.atlas.edge.authorization.api.model.RefreshTokenResponse;
import org.atlas.edge.authorization.core.UserDetailsImpl;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.domain.error.UserDomainError;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final OneTimeTokenService oneTimeTokenService;
  private final PasswordEncoder passwordEncoder;
  private final KvStoreService kvStoreService;

  @Transactional(readOnly = true)
  public LoginResponse login(LoginRequest request) throws Exception {
    Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
        request.getEmail(), request.getPassword());
    return doLogin(authenticationToken);
  }

  @Transactional(readOnly = true)
  public RefreshTokenResponse refreshToken(RefreshTokenRequest request) throws Exception {
    String userId = JwtUtil.extractSubjectVerified(request.getRefreshToken());

    // Reissue tokens
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));
    Principal principal = user.toPrincipal();
    String accessToken = JwtUtil.issueAccessToken(principal);
    String refreshToken = JwtUtil.issueRefreshToken(principal);
    return new RefreshTokenResponse(accessToken, refreshToken);
  }

  public void logout(String accessToken) throws Exception {
    if (StringUtil.isBlank(accessToken)) {
      throw new DomainException(CommonDomainError.UNAUTHORIZED, "Missing access token");
    }

    String hashedAccessToken = HashingUtil.sha256ToHex(accessToken);
    if (kvStoreService.exists(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME,
        hashedAccessToken)) {
      throw new DomainException(CommonDomainError.UNAUTHORIZED,
          "Access token has been already inactivated");
    }

    Date expiresAt = JwtUtil.extractExpiresAt(accessToken);
    long now = LegacyDateUtil.timestamp();
    long ttlMs = Math.max(1000L, expiresAt.getTime() - now);
    kvStoreService.put(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME, hashedAccessToken, "1",
        Duration.ofMillis(ttlMs));
  }

  @Transactional
  public void changePassword(ChangePasswordRequest request) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
      throw new DomainException(UserDomainError.WRONG_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    userRepository.update(user);
  }

  public LoginResponse oneTimeTokenLogin(OneTimeTokenLoginRequest request) throws Exception {
    Authentication authenticationToken = new OneTimeTokenAuthenticationToken(request.getToken());
    return doLogin(authenticationToken);
  }

  public GenerateOneTimeTokenResponse generateOneTimeToken(GenerateOneTimeTokenRequest request) {
    OneTimeToken token = oneTimeTokenService.generate(
        new org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest(
            request.getEmail()));
    return new GenerateOneTimeTokenResponse(token.getTokenValue());
  }

  private LoginResponse doLogin(Authentication authenticationToken) throws Exception {
    Authentication authentication = authenticationManager.authenticate(authenticationToken);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    User user = userRepository.findById(userDetails.getId())
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));
    Principal principal = user.toPrincipal();
    String accessToken = JwtUtil.issueAccessToken(principal);
    String refreshToken = JwtUtil.issueRefreshToken(principal);
    return new LoginResponse(accessToken, refreshToken);
  }
}
