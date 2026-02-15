package org.atlas.services.iam.port.in.user.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.user.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOutput {

  private String id;

  private String username;

  private String email;

  private String phoneNumber;

  private String firstName;

  private String lastName;

  private UserRole role;
}
