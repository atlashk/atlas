package org.atlas.services.iam.port.in.front.model;

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
public class ProfileOutput {

  private Integer userId;

  private String username;

  private String firstName;

  private String lastName;

  private String email;

  private String phoneNumber;

  private UserRole role;
}
