package org.atlas.domain.user.shared;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum Role {

  ADMIN("Administrator"),
  USER("User"),
  ;

  private final String description;
}
