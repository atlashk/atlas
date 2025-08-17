package org.atlas.infrastructure.auth.client.keycloak;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.auth-client.keycloak")
@Data
public class KeycloakProps {

  private String baseUrl;
  private String adminUsername;
  private String adminPassword;
  private String realmName;
}
