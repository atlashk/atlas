package org.atlas.platform.authorization.port.in.user.model.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class UpdateUserInput {

  private String id;

  private String firstName;

  private String lastName;

  @Builder.Default
  private UserRole role = UserRole.USER;
}
