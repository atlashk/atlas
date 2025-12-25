package org.atlas.infrastructure.iam.keycloak.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.auth.keycloak")
@Getter
@Setter
public class KeycloakProps {

  private String baseUrl;
  private String realm;
  private String adminUsername;
  private String adminPassword;
  private String clientId;
  private String clientSecret;
}
