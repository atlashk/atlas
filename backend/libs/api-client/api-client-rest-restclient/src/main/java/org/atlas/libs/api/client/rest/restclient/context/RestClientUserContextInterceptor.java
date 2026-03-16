package org.atlas.libs.api.client.rest.restclient.context;

import java.io.IOException;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityContext;
import org.atlas.libs.framework.security.CustomClaim;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RestClientUserContextInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    Principal principal = SecurityContext.get();
    if (principal != null) {
      if (principal.getUserId() != null) {
        request.getHeaders().add(CustomClaim.USER_ID.getHeader(), principal.getUserId());
      }
      if (principal.getUserRole() != null) {
        request.getHeaders().add(CustomClaim.USER_ROLE.getHeader(),
            principal.getUserRole().name());
      }
    }
    return execution.execute(request, body);
  }
}
