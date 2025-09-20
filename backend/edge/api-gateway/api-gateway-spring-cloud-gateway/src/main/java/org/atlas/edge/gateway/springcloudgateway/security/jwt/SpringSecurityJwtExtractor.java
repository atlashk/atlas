package org.atlas.edge.gateway.springcloudgateway.security.jwt;

import org.atlas.domain.user.shared.Role;
import org.atlas.framework.auth.enums.CustomClaim;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.api-gateway.auth-server", havingValue = "spring-security-jwt", matchIfMissing = true)
public class SpringSecurityJwtExtractor implements JwtExtractor {

  @Override
  public String extractUserId(Jwt jwt) {
    return jwt.getSubject();
  }

  @Override
  public Role extractUserRole(Jwt jwt) {
    String claim = jwt.getClaimAsString(CustomClaim.USER_ROLE.getClaim());
    if (claim == null) {
      throw new IllegalArgumentException(
          "Invalid JWT. Missing claim " + CustomClaim.USER_ROLE.getClaim());
    }
    return Role.valueOf(claim);
  }
}
