package org.atlas.domain.auth.usecase.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateUserInput {

  private Integer userId;
  private String username;
  private String password;
  private String email;
  private String phoneNumber;
  private Role role;
}
