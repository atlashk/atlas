package org.atlas.edge.gateway.springcloudgateway.security.keycloak;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.atlas.domain.user.shared.Role;
import org.atlas.edge.gateway.springcloudgateway.security.core.JwtExtractor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.api-gateway.auth-server", havingValue = "keycloak")
public class JwtExtractorImpl implements JwtExtractor {

  @Override
  public String extractUserId(Jwt jwt) {
    return jwt.getSubject();
  }

  @Override
  public Role extractUserRole(Jwt jwt) {
    Map<String, Object> realmAccess = jwt.getClaim("realm_access");
    List<String> roles =
        realmAccess != null && realmAccess.get("roles") instanceof List
            ? (List<String>) realmAccess.get("roles")
            : Collections.emptyList();
    for (String r : roles) {
      if ("ADMIN".equalsIgnoreCase(r)) {
        return Role.ADMIN;
      }
    }
    return Role.USER;
  }
}
