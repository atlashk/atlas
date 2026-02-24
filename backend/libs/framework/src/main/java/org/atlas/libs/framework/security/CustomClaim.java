package org.atlas.libs.framework.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CustomClaim {

  USER_ID("user_id", "X-User-Id", String.class),
  USER_ROLE("user_role", "X-User-Role", UserRole.class),
  ;

  private final String claimName;
  private final String header;
  private final Class<?> clazz;
}
