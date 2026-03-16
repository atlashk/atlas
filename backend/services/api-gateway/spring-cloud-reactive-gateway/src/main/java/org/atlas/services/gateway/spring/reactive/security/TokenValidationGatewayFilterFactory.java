package org.atlas.services.gateway.spring.reactive.security;

import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.cryptography.HashingUtil;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.kvstore.ReactiveKvStoreService;
import org.atlas.libs.framework.security.SecurityConstant;
import org.atlas.services.gateway.spring.reactive.util.HttpUtil;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
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

  private final ReactiveKvStoreService reactiveKvStoreService;

  public TokenValidationGatewayFilterFactory(ReactiveKvStoreService reactiveKvStoreService) {
    super(Config.class);
    this.reactiveKvStoreService = reactiveKvStoreService;
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
    String hashedToken = HashingUtil.sha256ToHex(jwt.getTokenValue());
    return reactiveKvStoreService.exists(SecurityConstant.TOKEN_BLACKLISTED_KV_STORE_NAME,
            hashedToken)
        .flatMap(isBlacklisted -> {
          if (isBlacklisted) {
            ApiResponseWrapper<Void> response = ApiResponseWrapper.error(
                CommonDomainError.UNAUTHORIZED.getErrorCode(), "Token has been inactivated");
            return HttpUtil.respond(exchange, response, HttpStatus.UNAUTHORIZED);
          }
          return chain.filter(exchange);
        });
  }

  public static class Config {
    // Add configuration properties if needed
  }
}
