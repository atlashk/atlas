package org.atlas.edge.gateway.springcloudgateway.security.jwt;

import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtExtractor {

  String extractUserId(Jwt jwt);
  String extractUserRoles(Jwt jwt);
}
