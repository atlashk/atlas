package org.atlas.domain.auth.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;
import org.atlas.framework.domain.entity.DomainEntity;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class AuthUser extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer userId;

  private String username;

  private String plainPassword;

  // Hashed password
  private String password;

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;

  private Role role;
}
