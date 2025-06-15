package org.atlas.edge.gateway.springcloudgateway.security.jwt;

import org.atlas.framework.auth.enums.CustomClaim;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.api-gateway.auth-server", havingValue = "spring-security-jwt", matchIfMissing = true)
public class SpringSecurityJwtExtractor implements JwtExtractor {

  @Override
  public String extractSessionId(Jwt jwt) {
    return jwt.getClaimAsString(CustomClaim.SESSION_ID.getClaim());
  }

  @Override
  public String extractUserId(Jwt jwt) {
    return jwt.getSubject();
  }

  @Override
  public String extractUserRoles(Jwt jwt) {
    return jwt.getClaimAsString(CustomClaim.USER_ROLES.getClaim());
  }
}
