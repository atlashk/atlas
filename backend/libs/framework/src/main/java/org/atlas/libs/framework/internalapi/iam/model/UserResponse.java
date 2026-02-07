package org.atlas.libs.framework.internalapi.iam.model;

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
public class UserResponse {

  private String id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private UserRole role;
}
