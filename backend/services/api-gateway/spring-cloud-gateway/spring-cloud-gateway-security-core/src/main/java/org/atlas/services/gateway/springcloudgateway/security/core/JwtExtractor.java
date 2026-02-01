package org.atlas.services.gateway.springcloudgateway.security.core;

import jakarta.annotation.Nullable;
import org.atlas.libs.framework.domain.user.UserRole;
import org.springframework.security.oauth2.jwt.Jwt;

public interface JwtExtractor {

  @Nullable
  String extractUserId(Jwt jwt);

  @Nullable
  UserRole extractUserRole(Jwt jwt);
}
