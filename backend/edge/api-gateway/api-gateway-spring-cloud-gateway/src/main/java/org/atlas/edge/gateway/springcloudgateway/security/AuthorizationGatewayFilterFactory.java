package org.atlas.edge.gateway.springcloudgateway.security;

import java.util.Arrays;
import java.util.List;
import lombok.Data;
import org.atlas.edge.gateway.springcloudgateway.security.jwt.JwtExtractor;
import org.atlas.edge.gateway.springcloudgateway.util.HttpUtil;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.error.AppError;
import org.atlas.framework.util.CollectionUtil;
import org.atlas.framework.util.StringUtil;
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
            .filter(auth -> auth != null && auth.isAuthenticated()
                && auth.getCredentials() instanceof Jwt)
            .map(auth -> (Jwt) auth.getCredentials())
            .flatMap(jwt -> checkRole(jwt, exchange, chain, config));
  }

  private Mono<Void> checkRole(Jwt jwt, ServerWebExchange exchange,
      GatewayFilterChain chain, Config config) {
    // Extract user roles from JWT claims
    String userRolesClaim = jwtExtractor.extractUserRoles(jwt);

    if (StringUtil.isNotBlank(userRolesClaim) && CollectionUtil.isNotEmpty(config.getRoles())) {
      List<String> userRoles = Arrays.asList(userRolesClaim.split(","));
      // Check if any role matches
      boolean hasRole = userRoles.stream().anyMatch(config.getRoles()::contains);
      if (hasRole) {
        return chain.filter(exchange);
      }
    }

    ApiResponseWrapper<Void> response = ApiResponseWrapper.error(
        AppError.FORBIDDEN.getErrorCode(), "Forbidden");
    return HttpUtil.respond(exchange, response, HttpStatus.FORBIDDEN);
  }

  @Data
  public static class Config {

    private List<String> roles;
  }
}
