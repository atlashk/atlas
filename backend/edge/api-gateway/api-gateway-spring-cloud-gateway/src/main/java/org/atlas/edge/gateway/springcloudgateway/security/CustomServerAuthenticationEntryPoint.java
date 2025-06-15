package org.atlas.edge.gateway.springcloudgateway.security;

import org.atlas.edge.gateway.springcloudgateway.util.HttpUtil;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.error.AppError;
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
        AppError.UNAUTHORIZED.getErrorCode(), ex.getMessage());
    return HttpUtil.respond(exchange, apiResponseWrapperBody, HttpStatus.UNAUTHORIZED);
  }
}
