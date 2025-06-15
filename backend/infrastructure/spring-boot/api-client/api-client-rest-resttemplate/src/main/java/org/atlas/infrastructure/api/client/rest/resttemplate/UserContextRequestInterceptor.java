package org.atlas.infrastructure.api.client.rest.resttemplate;

import java.io.IOException;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.auth.enums.CustomClaim;
import org.atlas.framework.util.RoleUtil;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class UserContextRequestInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo != null) {
      request.getHeaders().add(CustomClaim.SESSION_ID.getHeader(),
          contextInfo.getSessionId());
      request.getHeaders().add(CustomClaim.USER_ID.getHeader(),
          String.valueOf(contextInfo.getUserId()));
      request.getHeaders().add(CustomClaim.USER_ROLES.getHeader(),
          RoleUtil.toRolesString(contextInfo.getUserRoles()));
    }
    return execution.execute(request, body);
  }
}
