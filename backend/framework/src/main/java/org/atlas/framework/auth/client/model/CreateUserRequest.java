package org.atlas.framework.auth.client.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.atlas.domain.user.shared.enums.Role;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  private Integer userId;
  private String username;
  private String password;
  private String email;
  private String phoneNumber;
  private Role role;
}
