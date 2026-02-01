package org.atlas.services.iam.port.in.front.model;

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

  private String username;

  private String password;

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;
}
