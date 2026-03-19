package org.atlas.services.user.port.in.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.user.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class CreateUserInput {

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;

  private String password;

  @Builder.Default
  private UserRole role = UserRole.USER;
}
