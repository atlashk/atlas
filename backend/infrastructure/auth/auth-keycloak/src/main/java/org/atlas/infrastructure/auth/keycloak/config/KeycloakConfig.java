package org.atlas.infrastructure.auth.keycloak.config;

import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

  private final KeycloakProps keycloakProps;

  @Bean
  public Keycloak keycloak() {
    return Keycloak.getInstance(
        keycloakProps.getBaseUrl(),
        keycloakProps.getAdminRealm(),
        keycloakProps.getAdminUsername(),
        keycloakProps.getAdminPassword(),
        keycloakProps.getAdminClientId());
  }
}
