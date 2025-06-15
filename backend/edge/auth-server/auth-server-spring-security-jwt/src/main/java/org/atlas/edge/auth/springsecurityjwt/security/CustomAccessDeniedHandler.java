package org.atlas.edge.auth.springsecurityjwt.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.api.server.rest.response.ApiResponseWrapper;
import org.atlas.framework.error.AppError;
import org.atlas.infrastructure.api.server.rest.core.util.HttpUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

/**
 * AccessDecisionHandler is triggered when a user is authenticated but not authorized to access the
 * given resource.
 */
@Slf4j
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      AccessDeniedException exception)
      throws IOException, ServletException {
    if (response.isCommitted()) {
      log.info("ApiResponseWrapper has already been committed");
      return;
    }

    ApiResponseWrapper<Void> restApiResponseWrapper = ApiResponseWrapper.error(
        AppError.FORBIDDEN.getErrorCode(), exception.getMessage());
    HttpUtil.respondJson(response, restApiResponseWrapper, HttpStatus.FORBIDDEN);
  }
}
