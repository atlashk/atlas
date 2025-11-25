package org.atlas.infrastructure.auth.client.keycloak;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

  private final KeycloakProps keycloakProps;

  @Bean
  public Keycloak keycloakClient() {
    return Keycloak.getInstance(
        keycloakProps.getBaseUrl(),
        "master",
        keycloakProps.getAdminUsername(),
        keycloakProps.getAdminPassword(),
        "admin-cli");
  }
}
