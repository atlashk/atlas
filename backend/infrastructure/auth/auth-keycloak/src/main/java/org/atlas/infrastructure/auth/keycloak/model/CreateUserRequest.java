package org.atlas.infrastructure.auth.keycloak.model;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.atlas.domain.user.shared.Role;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserRequest {

  private String username;
  private String password;
  private String firstName;
  private String lastName;
  private String email;
  private Role role;
  // User profile attributes
  private Map<String, String> attributes;
}
