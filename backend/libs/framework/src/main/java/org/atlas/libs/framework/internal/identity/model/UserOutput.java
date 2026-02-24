package org.atlas.libs.framework.internal.identity.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.libs.framework.domain.shared.identity.UserRole;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserOutput {

  private String id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private UserRole role;
}
