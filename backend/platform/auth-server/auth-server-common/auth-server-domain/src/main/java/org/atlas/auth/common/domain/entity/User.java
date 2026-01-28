package org.atlas.auth.common.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.domain.common.entity.DomainEntity;
import org.atlas.common.framework.domain.user.Role;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class User extends DomainEntity {

  @EqualsAndHashCode.Include
  private Integer userId;

  private String username;

  // Hashed password
  private String password;

  private String email;

  private String phoneNumber;

  private Role role;
}
