package org.atlas.framework.domain.event.contract.user.model;

import lombok.Getter;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@Getter
@Setter
public class User {

  private Integer id;
  private String firstName;
  private String lastName;
  private String email;
  private String phoneNumber;
  private Role role;
}
