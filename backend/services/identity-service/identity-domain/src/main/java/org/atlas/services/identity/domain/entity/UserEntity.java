package org.atlas.services.identity.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.common.entity.DomainEntity;
import org.atlas.libs.framework.domain.identity.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false, onlyExplicitlyIncluded = true)
public class UserEntity extends DomainEntity {

  @EqualsAndHashCode.Include
  private String id;

  private String username;

  // Hashed password
  private String password;

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;

  private UserRole role;
}
