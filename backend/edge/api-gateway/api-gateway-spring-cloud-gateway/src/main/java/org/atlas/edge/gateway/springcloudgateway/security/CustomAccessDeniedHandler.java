package org.atlas.edge.gateway.springcloudgateway.security;

import org.atlas.edge.gateway.springcloudgateway.util.HttpUtil;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.error.AppError;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomAccessDeniedHandler implements ServerAccessDeniedHandler {

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
    ApiResponseWrapper<Void> apiResponseWrapperBody = ApiResponseWrapper.error(
        AppError.FORBIDDEN.getErrorCode(), denied.getMessage());
    return HttpUtil.respond(exchange, apiResponseWrapperBody, HttpStatus.FORBIDDEN);
  }
}
