package org.atlas.libs.framework.security.jwt;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.atlas.libs.framework.domain.shared.user.UserRole;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Claim {

  USER_ID("user_id", String.class),
  PREFERRED_USERNAME("preferred_username", String.class),
  GIVEN_NAME("given_name", String.class),
  FAMILY_NAME("family_name", String.class),
  FIRST_NAME("first_name", String.class),
  LAST_NAME("last_name", String.class),
  EMAIL("email", String.class),
  PHONE_NUMBER("phone_number", String.class),
  USER_ROLE("user_role", UserRole.class),
  ;

  private final String claimName;
  private final Class<?> clazz;
}
