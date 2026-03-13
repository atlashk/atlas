package org.atlas.libs.api.client.rest.restclient.context;

import java.io.IOException;
import org.atlas.libs.framework.context.ContextInfo;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.security.CustomClaim;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RestClientUserContextInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo != null) {
      if (contextInfo.getUserId() != null) {
        request.getHeaders().add(CustomClaim.USER_ID.getHeader(), contextInfo.getUserId());
      }
      if (contextInfo.getUserRole() != null) {
        request.getHeaders().add(CustomClaim.USER_ROLE.getHeader(),
            contextInfo.getUserRole().name());
      }
    }
    return execution.execute(request, body);
  }
}
