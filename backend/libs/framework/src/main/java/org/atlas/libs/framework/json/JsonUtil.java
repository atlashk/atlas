package org.atlas.libs.framework.json;

import lombok.experimental.UtilityClass;
import org.atlas.libs.framework.json.jackson.JacksonService;

/**
 * Implement Singleton pattern with Bill Pugh solution
 */
@UtilityClass
public class JsonUtil {

  public static JsonService getInstance() {
    return ServiceHolder.INSTANCE;
  }

  private static class ServiceHolder {

    private static final JsonService INSTANCE = new JacksonService();
  }
}
