package org.atlas.common.framework.context;

import lombok.Getter;
import lombok.Setter;
import org.atlas.common.framework.domain.user.Role;

@Getter
@Setter
public class ContextInfo {

  private Integer userId;
  private Role userRole;
  private String ipAddress;

  public boolean isAdmin() {
    return Role.ADMIN.equals(userRole);
  }

  public boolean isUser() {
    return Role.USER.equals(userRole);
  }
}
