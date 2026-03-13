package org.atlas.libs.api.client.rest.restclient.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.api-client.rest")
@Getter
@Setter
public class ApiClientRestProps {

  private Long connectTimeout;
  private Long readTimeout;
  private Ssl ssl;
  
  @Getter
  @Setter
  public static class Ssl {
    
    private Boolean enabled;
  }
}
