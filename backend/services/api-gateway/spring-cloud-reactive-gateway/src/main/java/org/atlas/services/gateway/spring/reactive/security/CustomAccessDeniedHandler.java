package org.atlas.services.gateway.spring.reactive.security;

import org.atlas.libs.framework.api.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.CommonDomainError;
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
        CommonDomainError.FORBIDDEN.getErrorCode(), denied.getMessage());
    return HttpUtil.respond(exchange, apiResponseWrapperBody, HttpStatus.FORBIDDEN);
  }
}
