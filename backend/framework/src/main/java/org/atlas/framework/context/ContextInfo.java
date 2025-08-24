package org.atlas.framework.context;

import lombok.Data;
import org.atlas.domain.user.shared.enums.Role;

@Data
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
