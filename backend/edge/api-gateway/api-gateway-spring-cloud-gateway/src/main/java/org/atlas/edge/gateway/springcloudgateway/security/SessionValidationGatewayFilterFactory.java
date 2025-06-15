package org.atlas.edge.gateway.springcloudgateway.security;

import java.time.Instant;
import org.atlas.edge.gateway.springcloudgateway.security.jwt.JwtExtractor;
import org.atlas.edge.gateway.springcloudgateway.util.HttpUtil;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.constant.SecurityConstant;
import org.atlas.framework.error.AppError;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class SessionValidationGatewayFilterFactory extends
    AbstractGatewayFilterFactory<SessionValidationGatewayFilterFactory.Config> {

  private final JwtExtractor jwtExtractor;
  private final ReactiveStringRedisTemplate reactiveRedisTemplate;

  public SessionValidationGatewayFilterFactory(JwtExtractor jwtExtractor,
      ReactiveStringRedisTemplate reactiveRedisTemplate) {
    super(Config.class);
    this.jwtExtractor = jwtExtractor;
    this.reactiveRedisTemplate = reactiveRedisTemplate;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      return ReactiveSecurityContextHolder.getContext()
          .map(SecurityContext::getAuthentication)
          .filter(auth -> auth != null && auth.isAuthenticated() && auth.getCredentials() instanceof Jwt)
          .map(auth -> (Jwt) auth.getCredentials())
          .flatMap(jwt -> validateSession(jwt, exchange, chain));
    };
  }

  private Mono<Void> validateSession(Jwt jwt, ServerWebExchange exchange, GatewayFilterChain chain) {
    // Extract userId and sessionId from JWT claims
    String sessionId = jwtExtractor.extractSessionId(jwt);
    String userId = jwtExtractor.extractUserId(jwt);

    // Build Redis keys for last logout timestamp and session blacklist
    String lastLogoutTsRedisKey = String.format(SecurityConstant.LAST_LOGOUT_TS_REDIS_KEY_FORMAT, userId);
    String sessionBlacklistedRedisKey = String.format(SecurityConstant.SESSION_BLACKLISTED_REDIS_KEY_FORMAT, sessionId);

    // Check if the JWT's issuedAt is before the user's last logout timestamp
    Mono<Boolean> invalidIssuedAt = reactiveRedisTemplate.opsForValue()
        .get(lastLogoutTsRedisKey)
        .map(lastLogoutTsStr -> {
          try {
            long lastLogoutTs = Long.parseLong(lastLogoutTsStr);
            // If the token was issued before last logout, session is invalid
            assert jwt.getIssuedAt() != null;
            return jwt.getIssuedAt().isBefore(Instant.ofEpochMilli(lastLogoutTs));
          } catch (NumberFormatException e) {
            // If parsing fails, treat session as valid
            return false;
          }
        })
        .defaultIfEmpty(false); // If no logout timestamp found, treat session as valid

    // Check if the session ID is explicitly blacklisted
    Mono<Boolean> sessionBlacklisted = reactiveRedisTemplate.hasKey(sessionBlacklistedRedisKey)
        .defaultIfEmpty(false); // If key doesn't exist, treat session as not blacklisted

    // Evaluate both conditions (logout timestamp and blacklist status)
    return Mono.zip(invalidIssuedAt, sessionBlacklisted)
        .flatMap(result -> {
          boolean isInvalidIssuedAt = result.getT1();
          boolean isSessionBlacklisted = result.getT2();

          // If either condition is true, reject the request
          if (isInvalidIssuedAt || isSessionBlacklisted) {
            ApiResponseWrapper<Void> response = ApiResponseWrapper.error(
                AppError.UNAUTHORIZED.getErrorCode(), "Session has been inactivated");
            return HttpUtil.respond(exchange, response, HttpStatus.UNAUTHORIZED);
          }

          // Otherwise, continue the request chain
          return chain.filter(exchange);
        });
  }

  public static class Config {
    // Add configuration properties if needed
  }
}
