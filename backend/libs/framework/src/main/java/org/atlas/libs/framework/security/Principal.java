package org.atlas.libs.framework.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Principal {

  private String userId;
  private String firstName;
  private String lastName;
  private String email;
  private String phone;
  private UserRole userRole;
  private String ipAddress;

  public boolean isAdmin() {
    return UserRole.ADMIN.equals(userRole);
  }

  public boolean isUser() {
    return UserRole.USER.equals(userRole);
  }
}
