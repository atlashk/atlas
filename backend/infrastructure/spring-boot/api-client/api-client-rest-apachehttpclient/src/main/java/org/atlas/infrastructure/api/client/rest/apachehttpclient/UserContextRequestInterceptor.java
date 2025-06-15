package org.atlas.infrastructure.api.client.rest.apachehttpclient;

import java.io.IOException;
import org.apache.hc.core5.http.EntityDetails;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpRequestInterceptor;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.auth.enums.CustomClaim;
import org.atlas.framework.util.RoleUtil;

public class UserContextRequestInterceptor implements HttpRequestInterceptor {

  @Override
  public void process(HttpRequest httpRequest, EntityDetails entityDetails, HttpContext httpContext)
      throws HttpException, IOException {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo != null) {
      httpRequest.addHeader(CustomClaim.SESSION_ID.getHeader(), contextInfo.getSessionId());
      httpRequest.addHeader(CustomClaim.USER_ID.getHeader(), contextInfo.getUserId());
      httpRequest.addHeader(CustomClaim.USER_ROLES.getHeader(),
          RoleUtil.toRolesString(contextInfo.getUserRoles()));
    }
  }
}
