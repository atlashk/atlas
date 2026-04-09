package org.atlas.libs.framework.domain.shared.user;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.atlas.libs.framework.domain.enums.ReferenceData;

@ReferenceData
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum UserRole {

  ADMIN("Administrator"),
  USER("User"),
  ;

  private final String description;
}
