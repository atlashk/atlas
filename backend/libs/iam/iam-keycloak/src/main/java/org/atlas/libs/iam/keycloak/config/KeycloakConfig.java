package org.atlas.libs.iam.keycloak.config;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.util.StringUtil;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class KeycloakConfig {

  private final KeycloakProps keycloakProps;

  /**
   * @return Keycloak admin client
   */
  @Bean
  public Keycloak keycloak() {
    KeycloakBuilder builder = KeycloakBuilder.builder()
        .serverUrl(keycloakProps.getBaseUrl())
        .realm(keycloakProps.getAdminRealm())
        .grantType(OAuth2Constants.PASSWORD)
        .clientId(keycloakProps.getAdminClientId())
        .username(keycloakProps.getAdminUsername())
        .password(keycloakProps.getAdminPassword());

    if (StringUtil.isNotBlank(keycloakProps.getAdminClientSecret())) {
      builder.clientSecret(keycloakProps.getAdminClientSecret());
    }

    return builder.build();
  }
}
