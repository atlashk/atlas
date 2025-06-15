package org.atlas.edge.auth.springsecurityjwt.security;

import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.json.JsonUtil;
import org.atlas.framework.util.RandomUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.ott.DefaultOneTimeToken;
import org.springframework.security.authentication.ott.GenerateOneTimeTokenRequest;
import org.springframework.security.authentication.ott.OneTimeToken;
import org.springframework.security.authentication.ott.OneTimeTokenAuthenticationToken;
import org.springframework.security.authentication.ott.OneTimeTokenService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OneTimeTokenServiceImpl implements OneTimeTokenService {

  private final RedisTemplate<String, Object> redisTemplate;

  private static final int TOKEN_BYTE_LENGTH = 32;
  private static final Duration TOKEN_TTL = Duration.ofMinutes(15);

  @Override
  public OneTimeToken generate(GenerateOneTimeTokenRequest request) {
    String tokenValue = RandomUtil.generateOneTimeToken(TOKEN_BYTE_LENGTH);
    OneTimeToken token = new DefaultOneTimeToken(
        tokenValue, request.getUsername(), Instant.now().plus(TOKEN_TTL));
    redisTemplate.opsForValue().set(redisKey(request.getUsername()), token, TOKEN_TTL);
    return token;
  }

  @Override
  public OneTimeToken consume(OneTimeTokenAuthenticationToken authenticationToken) {
    // Obtain token from Redis
    String identifier = (String) authenticationToken.getPrincipal();
    LinkedHashMap<?, ?> redisValue = (LinkedHashMap<?, ?>) redisTemplate.opsForValue().get(redisKey(identifier));
    OneTimeToken token = new DefaultOneTimeToken(
        (String) redisValue.get("tokenValue"),
        (String) redisValue.get("username"),
        Instant.parse((String) redisValue.get("expiresAt"))
    );

    // Validate token
    if (isValid(token)) {
      return null;
    }

    // Mark token as consumed by deleting it from Redis
    redisTemplate.delete(redisKey(identifier));

    return token;
  }

  private String redisKey(String username) {
    return String.format("oneTimeToken:%s", username);
  }

  private boolean isValid(OneTimeToken token) {
    return token == null ||
        Instant.now().isAfter(token.getExpiresAt()); // expired
  }
}
