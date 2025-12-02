package org.atlas.infrastructure.api.client.rest.resttemplate;

import java.io.IOException;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.security.CustomClaim;
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
      if (contextInfo.getUserId() != null) {
        request.getHeaders().add(CustomClaim.USER_ID.getHeader(),
            String.valueOf(contextInfo.getUserId()));
      }
      if (contextInfo.getUserRole() != null) {
        request.getHeaders().add(CustomClaim.USER_ROLE.getHeader(),
            contextInfo.getUserRole().name());
      }
    }
    return execution.execute(request, body);
  }
}
