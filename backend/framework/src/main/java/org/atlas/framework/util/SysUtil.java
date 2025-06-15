package org.atlas.framework.util;

import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SysUtil {

  /**
   * @return environment variable value
   */
  public static Optional<String> getEnv(String name) {
    return Optional.ofNullable(System.getenv(name));
  }
}
