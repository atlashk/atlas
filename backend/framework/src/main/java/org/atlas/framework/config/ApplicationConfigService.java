package org.atlas.framework.config;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ApplicationConfigService {

  private final Environment environment;

  public String getApplicationName() {
    return environment.getProperty("spring.application.name");
  }

  public String getActiveProfile() {
    String[] profiles = environment.getActiveProfiles();
    String[] filteredProfiles = Arrays.stream(profiles)
        .filter(profile -> !"kubernetes".equals(profile))
        .toArray(String[]::new);
    return filteredProfiles.length > 0 ? filteredProfiles[0] : "default";
  }

  public String getConfig(String key, String defaultValue) {
    return environment.getProperty(obtainFullKey(key), defaultValue);
  }

  public Integer getConfigAsInteger(String application, String key, Integer defaultValue) {
    return environment.getProperty(obtainFullKey(key), Integer.class, defaultValue);
  }

  public Long getConfigAsLong(String application, String key, Long defaultValue) {
    return environment.getProperty(obtainFullKey(key), Long.class, defaultValue);
  }

  public Double getConfigAsDouble(String application, String key, Double defaultValue) {
    return environment.getProperty(obtainFullKey(key), Double.class, defaultValue);
  }

  public BigDecimal getConfigAsBigDecimal(String application, String key,
      BigDecimal defaultValue) {
    return environment.getProperty(obtainFullKey(key), BigDecimal.class, defaultValue);
  }

  public boolean getConfigAsBoolean(String application, String key, boolean defaultValue) {
    return environment.getProperty(obtainFullKey(key), Boolean.class, defaultValue);
  }

  public List<String> getConfigAsList(String application, String key) {
    return Binder.get(environment)
        .bind(obtainFullKey(key), Bindable.listOf(String.class))
        .orElse(Collections.emptyList())
        .stream()
        .map(value -> value.replaceAll("^\\[|]$", "")) // remove leading '[' or trailing ']'
        .toList();
  }

  public <T> T getConfigAsClass(String application, String key, Class<T> clazz,
      T defaultValue) {
    return environment.getProperty(obtainFullKey(key), clazz, defaultValue);
  }

  private String obtainFullKey(String key) {
    return String.format("app.%s", key);
  }
}
