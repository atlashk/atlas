package org.atlas.services.user.port.in.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.user.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOutput {

  private String id;

  private String email;

  private String phone;

  private String firstName;

  private String lastName;

  private UserRole role;
}
