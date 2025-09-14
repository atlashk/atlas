package org.atlas.framework.config;

import java.math.BigDecimal;
import java.util.List;

public interface ApplicationConfigPort {

  String getApplicationName();

  String getActiveProfile();

  default String getConfig(String application, String key) {
    return getConfig(application, key, null);
  }

  String getConfig(String application, String key, String defaultValue);

  default Integer getConfigAsInteger(String application, String key) {
    return getConfigAsInteger(application, key, null);
  }

  Integer getConfigAsInteger(String application, String key, Integer defaultValue);

  default Long getConfigAsLong(String application, String key) {
    return getConfigAsLong(application, key, null);
  }

  Long getConfigAsLong(String application, String key, Long defaultValue);

  default Double getConfigAsDouble(String application, String key) {
    return getConfigAsDouble(application, key, null);
  }

  Double getConfigAsDouble(String application, String key, Double defaultValue);

  default BigDecimal getConfigAsBigDecimal(String application, String key) {
    return getConfigAsBigDecimal(application, key, null);
  }

  BigDecimal getConfigAsBigDecimal(String application, String key, BigDecimal defaultValue);

  default boolean getConfigAsBoolean(String application, String key) {
    return getConfigAsBoolean(application, key, false);
  }

  boolean getConfigAsBoolean(String application, String key, boolean defaultValue);

  List<String> getConfigAsList(String application, String key);

  default <T> T getConfigAsClass(String application, String key, Class<T> clazz) {
    return getConfigAsClass(application, key, clazz, null);
  }

  <T> T getConfigAsClass(String application, String key, Class<T> clazz, T defaultValue);
}
