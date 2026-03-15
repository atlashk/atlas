package org.atlas.platform.authorization.spring.application.core.oauth2;

import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {

  @Override
  public boolean supports(FederatedIdentityProvider provider) {
    return FederatedIdentityProvider.GOOGLE == provider;
  }
}
