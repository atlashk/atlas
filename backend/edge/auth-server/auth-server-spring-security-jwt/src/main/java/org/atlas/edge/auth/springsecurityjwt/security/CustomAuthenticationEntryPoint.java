package org.atlas.edge.auth.springsecurityjwt.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.infrastructure.api.server.rest.core.util.HttpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

/**
 * AuthenticationEntryPoint is triggered when an unauthenticated user requests a secured HTTP
 * resource.
 */
@Slf4j
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException exception)
      throws IOException {
    if (response.isCommitted()) {
      log.info("ApiResponseWrapper has already been committed");
      return;
    }

    ApiResponseWrapper<Void> restApiResponseWrapper = ApiResponseWrapper.error(HttpStatus.UNAUTHORIZED.value(),
        exception.getMessage());
    HttpUtil.respondJson(response, restApiResponseWrapper, HttpStatus.UNAUTHORIZED);
  }
}
