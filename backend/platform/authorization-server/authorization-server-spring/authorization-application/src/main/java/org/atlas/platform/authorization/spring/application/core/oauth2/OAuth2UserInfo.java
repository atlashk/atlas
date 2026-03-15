package org.atlas.platform.authorization.spring.application.core.oauth2;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.StringUtil;

@Builder
@Getter
@RequiredArgsConstructor
public class OAuth2UserInfo {

  private final String providerUserId;
  private final String email;
  private final String firstName;
  private final String lastName;

  public boolean isInvalid() {
    return StringUtil.isBlank(providerUserId) || StringUtil.isBlank(email);
  }
}
