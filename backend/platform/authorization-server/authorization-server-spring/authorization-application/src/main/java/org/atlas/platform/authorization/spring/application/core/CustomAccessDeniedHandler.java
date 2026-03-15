package org.atlas.platform.authorization.spring.application.core;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.api.server.rest.util.HttpUtil;
import org.atlas.libs.framework.api.server.rest.ApiResponseWrapper;
import org.atlas.libs.framework.domain.error.CommonDomainError;
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
        CommonDomainError.FORBIDDEN.getErrorCode(), exception.getMessage());
    HttpUtil.respondJson(response, restApiResponseWrapper, HttpStatus.FORBIDDEN);
  }
}
