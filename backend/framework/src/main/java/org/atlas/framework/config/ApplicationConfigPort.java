package org.atlas.framework.config;

import java.math.BigDecimal;
import java.util.List;

public interface ApplicationConfigPort {

  String getApplicationName();

  String getActiveProfile();

  default String getConfig(Application application, String key) {
    return getConfig(application, key, null);
  }

  String getConfig(Application application, String key, String defaultValue);

  default Integer getConfigAsInteger(Application application, String key) {
    return getConfigAsInteger(application, key, null);
  }

  Integer getConfigAsInteger(Application application, String key, Integer defaultValue);

  default Long getConfigAsLong(Application application, String key) {
    return getConfigAsLong(application, key, null);
  }

  Long getConfigAsLong(Application application, String key, Long defaultValue);

  default Double getConfigAsDouble(Application application, String key) {
    return getConfigAsDouble(application, key, null);
  }

  Double getConfigAsDouble(Application application, String key, Double defaultValue);

  default BigDecimal getConfigAsBigDecimal(Application application, String key) {
    return getConfigAsBigDecimal(application, key, null);
  }

  BigDecimal getConfigAsBigDecimal(Application application, String key, BigDecimal defaultValue);

  default boolean getConfigAsBoolean(Application application, String key) {
    return getConfigAsBoolean(application, key, false);
  }

  boolean getConfigAsBoolean(Application application, String key, boolean defaultValue);

  List<String> getConfigAsList(Application application, String key);

  default <T> T getConfigAsClass(Application application, String key, Class<T> clazz) {
    return getConfigAsClass(application, key, clazz, null);
  }

  <T> T getConfigAsClass(Application application, String key, Class<T> clazz, T defaultValue);
}
