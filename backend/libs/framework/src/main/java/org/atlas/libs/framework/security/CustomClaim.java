package org.atlas.libs.framework.security;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.atlas.libs.framework.domain.shared.user.UserRole;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum CustomClaim {

  USER_ID("user_id", String.class),
  FIRST_NAME("first_name", String.class),
  LAST_NAME("last_name", String.class),
  EMAIL("email", String.class),
  PHONE("phone", String.class),
  USER_ROLE("user_role", UserRole.class),
  ;

  private final String claimName;
  private final Class<?> clazz;
}
