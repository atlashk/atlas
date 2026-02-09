package org.atlas.services.iam.application.jwt.auth;

import java.time.Duration;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.libs.framework.util.DateUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.libs.jwt.IssueTokenInput;
import org.atlas.libs.jwt.JwtUtil;
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
  private final KvStoreService kvStoreService;

  @Override
  public Map<String, Object> jwkSet() throws Exception {
    return JwtUtil.jwkSet();
  }

  @Override
  public LoginOutput login(LoginInput input) throws Exception {
    Authentication authenticationToken = new UsernamePasswordAuthenticationToken(
        input.getUsername(), input.getPassword());
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
      throw new DomainException(DomainError.UNAUTHORIZED, "Missing access token");
    }

    String hashedAccessToken = HashingUtil.sha256ToHex(accessToken);
    if (kvStoreService.exists(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME,
        hashedAccessToken)) {
      throw new DomainException(DomainError.UNAUTHORIZED,
          "Access token has been already inactivated");
    }

    Date expiresAt = JwtUtil.extractExpiresAt(accessToken);
    long now = DateUtil.timestamp();
    long ttlMs = Math.max(1000L, expiresAt.getTime() - now);
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
    IssueTokenInput issueTokenInput = IssueTokenInput.builder()
        .userId(userDetails.getId())
        .role(userDetails.getRole())
        .build();
    String accessToken = JwtUtil.issueAccessToken(issueTokenInput);
    String refreshToken = JwtUtil.issueRefreshToken(issueTokenInput);
    return new LoginOutput(accessToken, refreshToken);
  }
}
