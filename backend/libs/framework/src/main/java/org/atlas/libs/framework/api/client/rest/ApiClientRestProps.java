package org.atlas.libs.framework.api.client.rest;

import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.api-client.rest")
@Getter
@Setter
public class ApiClientRestProps {

  private static final long DEFAULT_TIMEOUT_SECONDS = 30;

  private Long connectTimeout;
  private Long readTimeout;
  private Ssl ssl;

  @Getter
  @Setter
  public static class Ssl {

    private Boolean enabled;
  }

  public boolean isSslDisabled() {
    if (ssl == null) {
      return true;
    }
    return Boolean.FALSE.equals(ssl.getEnabled());
  }

  public Duration getConnectTimeoutDuration() {
    return resolveTimeoutDuration(connectTimeout);
  }

  public Duration getReadTimeoutDuration() {
    return resolveTimeoutDuration(readTimeout);
  }

  public int getConnectTimeoutMillis() {
    return resolveTimeoutMillis(connectTimeout);
  }

  public int getReadTimeoutMillis() {
    return resolveTimeoutMillis(readTimeout);
  }

  private Duration resolveTimeoutDuration(Long timeoutSeconds) {
    if (timeoutSeconds == null || timeoutSeconds <= 0) {
      return Duration.ofSeconds(DEFAULT_TIMEOUT_SECONDS);
    }
    return Duration.ofSeconds(timeoutSeconds);
  }

  private int resolveTimeoutMillis(Long timeoutSeconds) {
    if (timeoutSeconds == null || timeoutSeconds <= 0) {
      return 0;
    }
    return timeoutSeconds.intValue() * 1000;
  }
}
