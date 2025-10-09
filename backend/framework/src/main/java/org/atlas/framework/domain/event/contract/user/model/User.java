package org.atlas.framework.domain.event.contract.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class User {

  private Integer id;
  private String username;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;
}
