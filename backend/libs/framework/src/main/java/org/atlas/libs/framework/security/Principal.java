package org.atlas.libs.framework.security;

import java.util.Collections;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Principal {

  private String accessToken;
  private String userId;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private UserRole userRole;
  private String ipAddress;

  public boolean isAdmin() {
    return UserRole.ADMIN.equals(userRole);
  }

  public boolean isUser() {
    return UserRole.USER.equals(userRole);
  }

  public List<GrantedAuthority> getAuthorities() {
    return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + userRole.name()));
  }
}
