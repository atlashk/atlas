package org.atlas.infrastructure.config.yaml;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.framework.config.Application;
import org.atlas.framework.config.ApplicationConfigPort;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ApplicationConfigAdapter implements ApplicationConfigPort, InitializingBean {

  private static final String CONFIG_NODE_PREFIX = "app.config";

  private final Environment environment;

  private String activeProfile;

  @Override
  public void afterPropertiesSet() throws Exception {
    this.activeProfile = getActiveProfile();
  }

  @Override
  public String getApplicationName() {
    return environment.getProperty("spring.application.name");
  }

  @Override
  public String getActiveProfile() {
    String[] profiles = environment.getActiveProfiles();
    return profiles.length > 0 ? profiles[0] : "default";
  }

  @Override
  public String getConfig(Application application, String key, String defaultValue) {
    return environment.getProperty(toKey(application, key), defaultValue);
  }

  @Override
  public Integer getConfigAsInteger(Application application, String key, Integer defaultValue) {
    return environment.getProperty(toKey(application, key), Integer.class, defaultValue);
  }

  @Override
  public Long getConfigAsLong(Application application, String key, Long defaultValue) {
    return environment.getProperty(toKey(application, key), Long.class, defaultValue);
  }

  @Override
  public Double getConfigAsDouble(Application application, String key, Double defaultValue) {
    return environment.getProperty(toKey(application, key), Double.class, defaultValue);
  }

  @Override
  public BigDecimal getConfigAsBigDecimal(Application application, String key,
      BigDecimal defaultValue) {
    return environment.getProperty(toKey(application, key), BigDecimal.class, defaultValue);
  }

  @Override
  public boolean getConfigAsBoolean(Application application, String key, boolean defaultValue) {
    return environment.getProperty(toKey(application, key), Boolean.class, defaultValue);
  }

  @Override
  public List<String> getConfigAsList(Application application, String key) {
    return Binder.get(environment)
        .bind(toKey(application, key), Bindable.listOf(String.class))
        .orElse(Collections.emptyList())
        .stream()
        .map(value -> value.replaceAll("^\\[|]$", "")) // remove leading '[' or trailing ']'
        .toList();
  }

  @Override
  public <T> T getConfigAsClass(Application application, String key, Class<T> clazz,
      T defaultValue) {
    return environment.getProperty(toKey(application, key), clazz, defaultValue);
  }

  private String toKey(Application application, String key) {
    return String.format("%s.%s.%s.%s",
        CONFIG_NODE_PREFIX, activeProfile, application.getValue(), key);
  }
}
