package org.atlas.common.framework.internalapi.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.common.framework.domain.user.Role;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

  private Integer id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;
}
