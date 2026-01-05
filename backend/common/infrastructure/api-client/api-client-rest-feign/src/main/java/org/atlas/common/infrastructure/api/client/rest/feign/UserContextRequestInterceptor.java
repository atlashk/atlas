package org.atlas.common.infrastructure.api.client.rest.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.atlas.common.framework.context.ContextInfo;
import org.atlas.common.framework.context.Contexts;
import org.atlas.common.framework.security.CustomClaim;
import org.springframework.stereotype.Component;

@Component
public class UserContextRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate requestTemplate) {
    ContextInfo contextInfo = Contexts.get();
    if (contextInfo != null) {
      if (contextInfo.getUserId() != null) {
        requestTemplate.header(CustomClaim.USER_ID.getHeader(),
            String.valueOf(contextInfo.getUserId()));
      }
      if (contextInfo.getUserRole() != null) {
        requestTemplate.header(CustomClaim.USER_ROLE.getHeader(),
            contextInfo.getUserRole().name());
      }
    }
  }
}
