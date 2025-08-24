package org.atlas.framework.jwt;

import lombok.experimental.UtilityClass;
import org.atlas.framework.jwt.auth0.Auth0JwtService;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
@UtilityClass
public class JwtUtil {

  public static JwtService getInstance() {
    return ServiceHolder.INSTANCE;
  }

  private static class ServiceHolder {

    private static final JwtService INSTANCE = new Auth0JwtService();
  }
}
