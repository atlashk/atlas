package org.atlas.services.gateway.springcloudgateway.filter;

import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.services.gateway.springcloudgateway.security.core.HttpUtil;
import org.atlas.services.gateway.springcloudgateway.security.core.JwtExtractor;
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
public class AuthorizationGatewayFilterFactory extends
    AbstractGatewayFilterFactory<AuthorizationGatewayFilterFactory.Config> {

  private final JwtExtractor jwtExtractor;

  public AuthorizationGatewayFilterFactory(JwtExtractor jwtExtractor) {
    super(Config.class);
    this.jwtExtractor = jwtExtractor;
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
            .flatMap(jwt -> checkRole(jwt, exchange, chain, config));
  }

  private Mono<Void> checkRole(Jwt jwt, ServerWebExchange exchange,
      GatewayFilterChain chain, Config config) {
    // Extract user roles from JWT claims
    UserRole userRole = jwtExtractor.extractUserRole(jwt);

    if (CollectionUtil.isNotEmpty(config.getRoles())) {
      boolean hasRole = config.getRoles()
          .stream()
          .anyMatch(configRole -> userRole.name().equals(configRole));
      if (hasRole) {
        return chain.filter(exchange);
      }
      ApiResponseWrapper<Void> response = ApiResponseWrapper.error(
          DomainError.FORBIDDEN.getErrorCode(), "Forbidden");
      return HttpUtil.respond(exchange, response, HttpStatus.FORBIDDEN);
    }

    return chain.filter(exchange);
  }

  @Override
  public List<String> shortcutFieldOrder() {
    return Collections.singletonList("roles");
  }

  @Getter
  @Setter
  public static class Config {

    private List<String> roles;
  }
}
