package org.atlas.platform.authorization.spring.application.authentication;

import java.time.Duration;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.LegacyDateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.jwt.IssueTokenInput;
import org.atlas.libs.jwt.JwtUtil;
import org.atlas.platform.authorization.spring.application.core.UserDetailsImpl;
import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.domain.error.DomainError;
import org.atlas.platform.authorization.domain.exception.DomainException;
import org.atlas.platform.authorization.port.in.authentication.model.ChangePasswordInput;
import org.atlas.platform.authorization.port.in.authentication.model.GenerateOneTimeTokenInput;
import org.atlas.platform.authorization.port.in.authentication.model.GenerateOneTimeTokenOutput;
import org.atlas.platform.authorization.port.in.authentication.model.LoginInput;
import org.atlas.platform.authorization.port.in.authentication.model.LoginOutput;
import org.atlas.platform.authorization.port.in.authentication.model.OneTimeTokenLoginInput;
import org.atlas.platform.authorization.port.in.authentication.model.RefreshTokenInput;
import org.atlas.platform.authorization.port.in.authentication.model.RefreshTokenOutput;
import org.atlas.platform.authorization.port.in.authentication.service.AuthenticationService;
import org.atlas.platform.authorization.port.out.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
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
public class AuthenticationServiceImpl implements AuthenticationService {

  private final UserRepository userRepository;
  private final AuthenticationManager authenticationManager;
  private final OneTimeTokenService oneTimeTokenService;
  private final PasswordEncoder passwordEncoder;
  private final KvStoreService kvStoreService;

  @Override
  @Transactional(readOnly = true)
  public LoginOutput login(LoginInput input) throws Exception {
    Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
        input.getEmail(), input.getPassword());
    return doLogin(authenticationToken);
  }

  @Override
  @Transactional(readOnly = true)
  public RefreshTokenOutput refreshToken(RefreshTokenInput input) throws Exception {
    // Parse refresh token and extract userId (subject)
    String userId = JwtUtil.extractSubject(input.getRefreshToken());

    // Reissue tokens
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
    IssueTokenInput issueTokenInput = IssueTokenInput.builder()
        .userId(user.getId())
        .role(user.getRole())
        .build();
    String accessToken = JwtUtil.issueAccessToken(issueTokenInput);
    String refreshToken = JwtUtil.issueRefreshToken(issueTokenInput);

    return new RefreshTokenOutput(accessToken, refreshToken);
  }

  @Override
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

  @Override
  @Transactional
  public void changePassword(ChangePasswordInput input) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    if (!passwordEncoder.matches(input.getOldPassword(), user.getPassword())) {
      throw new DomainException(DomainError.WRONG_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(input.getNewPassword()));
    userRepository.update(user);
  }

  @Override
  public LoginOutput oneTimeTokenLogin(OneTimeTokenLoginInput input) throws Exception {
    Authentication authenticationToken = new OneTimeTokenAuthenticationToken(input.getToken());
    return doLogin(authenticationToken);
  }

  @Override
  public GenerateOneTimeTokenOutput generateOneTimeToken(GenerateOneTimeTokenInput input) {
    OneTimeToken token = oneTimeTokenService.generate(
        new GenerateOneTimeTokenRequest(input.getEmail()));
    return new GenerateOneTimeTokenOutput(token.getTokenValue());
  }

  private LoginOutput doLogin(Authentication authenticationToken) throws Exception {
    Authentication authentication = authenticationManager.authenticate(authenticationToken);
    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
    IssueTokenInput issueTokenInput = IssueTokenInput.builder()
        .userId(userDetails.getId())
        .role(userDetails.getRole())
        .build();
    String accessToken = JwtUtil.issueAccessToken(issueTokenInput);
    String refreshToken = JwtUtil.issueRefreshToken(issueTokenInput);
    return new LoginOutput(accessToken, refreshToken);
  }
}
