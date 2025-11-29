package org.atlas.edge.gateway.springcloudgateway.filter;

import org.atlas.edge.gateway.springcloudgateway.security.core.JwtExtractor;
import org.atlas.framework.security.CustomClaim;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

@Component
public class TokenRelayGatewayFilterFactory extends
    AbstractGatewayFilterFactory<TokenRelayGatewayFilterFactory.Config> {

  private final JwtExtractor jwtExtractor;

  public TokenRelayGatewayFilterFactory(JwtExtractor jwtExtractor) {
    super(Config.class);
    this.jwtExtractor = jwtExtractor;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .filter(auth -> auth != null &&
            auth.isAuthenticated() &&
            auth.getCredentials() != null &&
            auth.getCredentials() instanceof Jwt)
        .map(auth -> (Jwt) auth.getCredentials())
        .flatMap(jwt -> {
          // Mutate the request to add custom headers
          ServerHttpRequest mutatedRequest = exchange.getRequest()
              .mutate()
              .headers(httpHeaders -> {
                httpHeaders.remove(HttpHeaders.AUTHORIZATION);
                httpHeaders.set(CustomClaim.USER_ID.getHeader(),
                    jwtExtractor.extractUserId(jwt));
                httpHeaders.set(CustomClaim.USER_ROLE.getHeader(),
                    jwtExtractor.extractUserRole(jwt).name());
              })
              .build();
          ServerWebExchange mutatedExchange = exchange.mutate()
              .request(mutatedRequest)
              .build();
          return chain.filter(mutatedExchange);
        })
        .switchIfEmpty(chain.filter(exchange)); // Skip token relay if no JWT token
  }

  public static class Config {
    // Configuration properties if needed
  }
}
