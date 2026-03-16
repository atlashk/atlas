package org.atlas.libs.api.client.rest.resttemplate.context;

import java.io.IOException;
import org.atlas.libs.framework.security.Principal;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class RestClientUserContextInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body,
      ClientHttpRequestExecution execution) throws IOException {
    Principal principal = SecurityContextUtil.getPrincipal();
    if (principal != null) {
      request.getHeaders().set(HttpHeaders.AUTHORIZATION, "Bearer " + principal.getAccessToken());
    }
    return execution.execute(request, body);
  }
}
