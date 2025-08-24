package org.atlas.edge.gateway.springcloudgateway.security.jwt;

import org.atlas.domain.user.shared.enums.Role;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtExtractor {

  String extractUserId(Jwt jwt);

  Role extractUserRole(Jwt jwt);
}
