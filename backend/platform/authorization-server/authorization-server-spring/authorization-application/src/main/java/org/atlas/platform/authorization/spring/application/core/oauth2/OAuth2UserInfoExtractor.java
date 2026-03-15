package org.atlas.platform.authorization.spring.application.core.oauth2;

import org.atlas.libs.framework.security.FederatedIdentityProvider;
import org.springframework.security.oauth2.core.user.OAuth2User;

public interface OAuth2UserInfoExtractor {

  boolean supports(FederatedIdentityProvider provider);

  /**
   * Based on OIDC specs
   */
  default OAuth2UserInfo extract(OAuth2User oAuth2User) {
    return OAuth2UserInfo.builder()
        .providerUserId(oAuth2User.getAttribute("sub"))
        .firstName(oAuth2User.getAttribute("given_name"))
        .lastName(oAuth2User.getAttribute("family_name"))
        .email(oAuth2User.getAttribute("email"))
        .build();
  }
}
