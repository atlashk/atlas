package org.atlas.libs.framework.jwks;

import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.jwks.nimbus.NimbusJwkSetService;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
@UtilityClass
public class JwkSetUtil {

  public static JwkSetService getInstance() {
    return ServiceHolder.INSTANCE;
  }

  private static class ServiceHolder {

    private static final JwkSetService INSTANCE = new NimbusJwkSetService();
  }
}
