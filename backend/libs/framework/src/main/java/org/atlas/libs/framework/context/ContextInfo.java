package org.atlas.libs.framework.context;

import lombok.Getter;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;

@Getter
@Setter
public class ContextInfo {

  private String userId;
  private UserRole userRole;
  private String ipAddress;

  public boolean isAdmin() {
    return UserRole.ADMIN.equals(userRole);
  }

  public boolean isUser() {
    return UserRole.USER.equals(userRole);
  }
}
