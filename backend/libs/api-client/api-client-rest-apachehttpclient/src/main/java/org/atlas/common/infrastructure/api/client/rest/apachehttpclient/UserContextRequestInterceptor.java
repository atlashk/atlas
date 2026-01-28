package org.atlas.common.infrastructure.api.client.rest.apachehttpclient;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.atlas.common.framework.context.ContextInfo;
import org.atlas.common.framework.context.Contexts;
import org.atlas.common.framework.security.CustomClaim;

public class UserContextRequestInterceptor implements HttpRequestInterceptor {

  @Override
  public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext)
      throws HttpException, IOException {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo != null) {
      if (contextInfo.getUserId() != null) {
        httpRequest.addHeader(CustomClaim.USER_ID.getHeader(),
            String.valueOf(contextInfo.getUserId()));
      }
      if (contextInfo.getUserRole() != null) {
        httpRequest.addHeader(CustomClaim.USER_ROLE.getHeader(),
            contextInfo.getUserRole().name());
      }
    }
  }
}
