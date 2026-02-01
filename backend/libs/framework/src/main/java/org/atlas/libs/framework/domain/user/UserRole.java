package org.atlas.libs.framework.domain.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum UserRole {

  ADMIN("Administrator"),
  USER("User"),
  ;

  private final String description;
}
