package org.atlas.domain.user.entity;

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
public class User extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer id;

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
