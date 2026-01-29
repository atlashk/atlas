package org.atlas.services.gateway.springcloudgateway.security.core;

import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Used for Spring WebFlux
 */
@Component
public class CustomServerAuthenticationEntryPoint implements ServerAuthenticationEntryPoint {

  @Override
  public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
    ApiResponseWrapper<Void> apiResponseWrapperBody = ApiResponseWrapper.error(
        DomainError.UNAUTHORIZED.getErrorCode(), ex.getMessage());
    return HttpUtil.respond(exchange, apiResponseWrapperBody, HttpStatus.UNAUTHORIZED);
  }
}
