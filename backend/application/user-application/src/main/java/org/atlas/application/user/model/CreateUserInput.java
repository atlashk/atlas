package org.atlas.application.user.model;

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

  private String username;

  private String password;

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;

  @Builder.Default
  private Role role = Role.USER;
}
