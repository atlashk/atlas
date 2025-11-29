package org.atlas.edge.gateway.springcloudgateway.security.springsecurityjwt;

import java.util.Optional;
import org.atlas.domain.user.shared.Role;
import org.atlas.edge.gateway.springcloudgateway.security.core.JwtExtractor;
import org.atlas.framework.security.CustomClaim;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class JwtExtractorImpl implements JwtExtractor {

  @Override
  public String extractUserId(Jwt jwt) {
    return Optional.ofNullable(jwt.getSubject())
        .orElseThrow(() -> new IllegalArgumentException(""));
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
