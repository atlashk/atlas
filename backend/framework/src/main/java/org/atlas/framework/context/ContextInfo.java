package org.atlas.framework.context;

import java.util.Date;
import java.util.Set;
import lombok.Data;
import org.atlas.domain.user.shared.enums.Role;

@Data
public class ContextInfo {

  private String sessionId;
  private Integer userId;
  private Set<Role> userRoles;
  private String deviceId;
  private Date expiresAt;

  public boolean isAdmin() {
    return userRoles != null && userRoles.contains(Role.ADMIN);
  }

  public boolean isUser() {
    return userRoles != null && userRoles.contains(Role.USER);
  }
}
