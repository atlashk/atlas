package org.atlas.framework.context;

import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@Getter
@Setter
public class ContextInfo {

  private Integer userId;
  private Role userRole;

  public boolean isAdmin() {
    return Role.ADMIN.equals(userRole);
  }

  public boolean isUser() {
    return Role.USER.equals(userRole);
  }
}
