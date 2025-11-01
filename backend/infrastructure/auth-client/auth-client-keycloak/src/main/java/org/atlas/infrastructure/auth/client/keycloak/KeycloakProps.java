package org.atlas.infrastructure.auth.client.keycloak;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.auth-client.keycloak")
@Getter
@Setter
public class KeycloakProps {

  private String baseUrl;
  private String adminUsername;
  private String adminPassword;
  private String realmName;
}
