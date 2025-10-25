package org.atlas.edge.gateway.springcloudgateway.security.filter;

import java.time.Instant;
import org.atlas.edge.gateway.springcloudgateway.security.jwt.JwtExtractor;
import org.atlas.edge.gateway.springcloudgateway.util.HttpUtil;
import org.atlas.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.framework.security.SecurityConstant;
import org.atlas.framework.domain.error.DomainError;
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
public class TokenValidationGatewayFilterFactory extends
    AbstractGatewayFilterFactory<TokenValidationGatewayFilterFactory.Config> {

  private final JwtExtractor jwtExtractor;
  private final ReactiveStringRedisTemplate reactiveRedisTemplate;

  public TokenValidationGatewayFilterFactory(JwtExtractor jwtExtractor,
      ReactiveStringRedisTemplate reactiveRedisTemplate) {
    super(Config.class);
    this.jwtExtractor = jwtExtractor;
    this.reactiveRedisTemplate = reactiveRedisTemplate;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) ->
        ReactiveSecurityContextHolder.getContext()
            .map(SecurityContext::getAuthentication)
            .filter(auth -> auth != null &&
                auth.isAuthenticated() &&
                auth.getCredentials() != null &&
                auth.getCredentials() instanceof Jwt)
            .map(auth -> (Jwt) auth.getCredentials())
            .flatMap(jwt -> validateToken(jwt, exchange, chain))
            .switchIfEmpty(chain.filter(exchange)); // Skip validation if no JWT token
  }

  private Mono<Void> validateToken(Jwt jwt, ServerWebExchange exchange,
      GatewayFilterChain chain) {
    // Extract userId from JWT claims
    String userId = jwtExtractor.extractUserId(jwt);

    // Build Redis key for last logout timestamp
    String lastLogoutTsRedisKey = String.format(SecurityConstant.LAST_LOGOUT_TS_REDIS_KEY_FORMAT,
        userId);

    // Check if the JWT's issuedAt is before the user's last logout timestamp
    Mono<Boolean> invalidIssuedAt = reactiveRedisTemplate.opsForValue()
        .get(lastLogoutTsRedisKey)
        .map(lastLogoutTsStr -> {
          try {
            long lastLogoutTs = Long.parseLong(lastLogoutTsStr);
            assert jwt.getIssuedAt() != null;
            // If the token was issued before last logout, session is invalid
            return jwt.getIssuedAt().isBefore(Instant.ofEpochMilli(lastLogoutTs));
          } catch (NumberFormatException e) {
            // If parsing fails, treat session as valid
            return false;
          }
        })
        .defaultIfEmpty(false); // If no logout timestamp found, treat session as valid

    return invalidIssuedAt.flatMap(isInvalidIssuedAt -> {
      if (isInvalidIssuedAt) {
        ApiResponseWrapper<Void> response = ApiResponseWrapper.error(
            DomainError.UNAUTHORIZED.getErrorCode(), "Token has been inactivated");
        return HttpUtil.respond(exchange, response, HttpStatus.UNAUTHORIZED);
      }
      return chain.filter(exchange);
    });
  }

  public static class Config {
    // Add configuration properties if needed
  }
}
