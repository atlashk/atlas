package org.atlas.services.identity.application.spring.core.oauth2;

import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class GoogleOAuth2UserInfoExtractor implements OAuth2UserInfoExtractor {

  @Override
  public boolean supports(FederatedIdentityProvider provider) {
    return FederatedIdentityProvider.GOOGLE == provider;
  }
}
