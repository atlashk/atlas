package org.atlas.edge.gateway.springcloudgateway.ratelimiter;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.shared.enums.Role;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.gateway.filter.ratelimit.RateLimiter;
import org.springframework.cloud.gateway.filter.ratelimit.RedisRateLimiter;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@Primary
@Slf4j
public class UserRateLimiter implements RateLimiter<String> {

  private final RedisRateLimiter anonymousRedisRateLimiter;
  private final RedisRateLimiter userRedisRateLimiter;

  public UserRateLimiter(
      @Qualifier("anonymousRedisRateLimiter") RedisRateLimiter anonymousRedisRateLimiter,
      @Qualifier("userRedisRateLimiter") RedisRateLimiter userRedisRateLimiter) {
    this.anonymousRedisRateLimiter = anonymousRedisRateLimiter;
    this.userRedisRateLimiter = userRedisRateLimiter;
  }

  @Override
  public Mono<Response> isAllowed(String routeId, String key) {
    String normalizedKey = key != null ? key.toLowerCase() : "anonymous";
    if (normalizedKey.startsWith(Role.ADMIN.name().toLowerCase())) {
      // Skip limiting for admin
      return Mono.just(new Response(true, Map.of("X-RateLimit-Skip", "true")));
    } else if (normalizedKey.startsWith(Role.USER.name().toLowerCase())) {
      return userRedisRateLimiter.isAllowed(routeId, key);
    } else {
      return anonymousRedisRateLimiter.isAllowed(routeId, key);
    }
  }

  @Override
  public Class<String> getConfigClass() {
    return null;
  }

  @Override
  public String newConfig() {
    return "";
  }

  @Override
  public Map<String, String> getConfig() {
    return Map.of();
  }
}
