package org.atlas.edge.gateway.springcloudgateway.security.keycloak;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.atlas.domain.user.shared.Role;
import org.atlas.edge.gateway.springcloudgateway.security.core.JwtExtractor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class JwtExtractorImpl implements JwtExtractor {

  @Override
  public String extractUserId(Jwt jwt) {
    return jwt.getClaim("user_id").toString();
  }

  @Override
  public Role extractUserRole(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    List<String> roles =
        realmAccess != null && realmAccess.get("roles") instanceof List
            ? (List<String>) realmAccess.get("roles")
            : Collections.emptyList();
    for (String role : roles) {
      if (role.equalsIgnoreCase(Role.ADMIN.name())) {
        return Role.ADMIN;
      }
    }
    return Role.USER;
  }
}
