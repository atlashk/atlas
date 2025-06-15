package org.atlas.framework.jwt;

import lombok.experimental.UtilityClass;
import org.atlas.framework.jwt.auth0.Auth0JwtService;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
@UtilityClass
public class JwtUtil {

  private static class ServiceHolder {

    private static final JwtService INSTANCE = new Auth0JwtService();
  }

  public static JwtService getInstance() {
    return ServiceHolder.INSTANCE;
  }
}
