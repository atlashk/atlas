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
public class AdminUpdateUserInput {

  private String id;

  private String firstName;

  private String lastName;

  @Builder.Default
  private UserRole role = UserRole.USER;
}
