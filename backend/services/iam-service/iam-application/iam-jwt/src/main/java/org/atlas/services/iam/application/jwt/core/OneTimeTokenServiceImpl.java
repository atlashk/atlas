package org.atlas.services.iam.application.jwt.core;

import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.kvstore.KvStoreService;
import org.atlas.libs.framework.random.RandomUtil;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OneTimeTokenServiceImpl implements OneTimeTokenService {

  private static final String STORE_NAME = "one_time_token";
  private static final int TOKEN_BYTE_LENGTH = 32;
  private static final Duration TOKEN_TTL = Duration.ofMinutes(15);
  private final KvStoreService kvStoreService;

  @Override
  public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
    String tokenValue = RandomUtil.randomOneTimeToken(TOKEN_BYTE_LENGTH);
    OneTimeToken token = new DefaultOneTimeToken(
        tokenValue, request.getUsername(), Instant.now().plus(TOKEN_TTL));
    kvStoreService.put(STORE_NAME, request.getUsername(), token, TOKEN_TTL);
    return token;
  }

  @Override
  public OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
    String username = (String) authenticationToken.getPrincipal();
    String presentedTokenValue = (String) authenticationToken.getCredentials();
    if (presentedTokenValue == null) {
      return null;
    }

    OneTimeToken oneTimeToken = kvStoreService.get(STORE_NAME, username, OneTimeToken.class)
        .orElse(null);
    if (isInvalid(oneTimeToken) || !presentedTokenValue.equals(oneTimeToken.getTokenValue())) {
      return null;
    }

    kvStoreService.delete(STORE_NAME, username);

    return oneTimeToken;
  }

  private boolean isInvalid(OneTimeToken token) {
    return token == null || Instant.now().isAfter(token.getExpiresAt()); // expired
  }
}
