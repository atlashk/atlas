package org.atlas.framework.auth.client.model;

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
public class CreateAuthUserRequest {

  private Integer userId;
  private String username;
  private String password;
  private String email;
  private String phoneNumber;
  private Role role;
}
