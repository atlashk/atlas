package org.atlas.platform.authorization.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.entity.DomainEntity;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.Principal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class UserEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  // Hashed password
  private String password;

  private UserRole role;

  public Principal toPrincipal() {
    return Principal.builder()
        .userId(id)
        .firstName(firstName)
        .lastName(lastName)
        .email(email)
        .phone(phone)
        .userRole(role)
        .build();
  }
}
