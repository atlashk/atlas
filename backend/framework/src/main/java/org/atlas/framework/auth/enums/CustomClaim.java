package org.atlas.framework.auth.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CustomClaim {

  USER_ID("user_id", "X-User-Id"),
  USER_ROLE("user_role", "X-User-Role"),
  ;

  private final String claim;
  private final String header;
}
