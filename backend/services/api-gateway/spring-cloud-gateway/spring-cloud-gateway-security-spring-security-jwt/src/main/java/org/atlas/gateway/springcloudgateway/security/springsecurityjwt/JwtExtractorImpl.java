package org.atlas.gateway.springcloudgateway.security.springsecurityjwt;

import java.util.Optional;
import org.atlas.common.framework.domain.user.Role;
import org.atlas.gateway.springcloudgateway.security.core.JwtExtractor;
import org.atlas.common.framework.security.CustomClaim;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class JwtExtractorImpl implements JwtExtractor {

  @Override
  public String extractUserId(Jwt jwt) {
    return Optional.ofNullable(jwt.getSubject())
        .orElseThrow(() -> new IllegalArgumentException("Invalid JWT. Missing subject"));
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
