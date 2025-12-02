package org.atlas.framework.internalapi.auth.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  private Integer userId;
  private String username;
  private String password;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;
}
