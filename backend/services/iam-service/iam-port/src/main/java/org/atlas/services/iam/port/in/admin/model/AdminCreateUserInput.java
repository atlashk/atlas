package org.atlas.services.iam.port.in.admin.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class AdminCreateUserInput {

  private String username;

  private String password;

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;

  @Builder.Default
  private UserRole role = UserRole.USER;
}
