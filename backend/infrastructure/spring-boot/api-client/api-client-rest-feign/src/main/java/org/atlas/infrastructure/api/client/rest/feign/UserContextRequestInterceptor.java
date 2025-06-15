package org.atlas.infrastructure.api.client.rest.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.context.ContextInfo;
import org.atlas.framework.auth.enums.CustomClaim;
import org.atlas.framework.util.RoleUtil;
import org.springframework.stereotype.Component;

@Component
public class UserContextRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo != null) {
      requestTemplate.header(CustomClaim.USER_ID.getHeader(),
          String.valueOf(contextInfo.getUserId()));
      requestTemplate.header(CustomClaim.USER_ROLES.getHeader(),
          RoleUtil.toRolesString(contextInfo.getUserRoles()));
      requestTemplate.header(CustomClaim.SESSION_ID.getHeader(),
          contextInfo.getSessionId());
    }
  }
}
