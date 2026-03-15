package org.atlas.platform.authorization.port.in.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RegisterInput {

  private String firstName;

  private String lastName;

  private String email;

  private String phone;

  private String password;
}
