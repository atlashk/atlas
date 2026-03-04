package org.atlas.services.identity.application.keycloak.core.client;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.realm_role")
public class KeycloakRealmRoleClient {

  private final KeycloakProps keycloakProps;
  private final KeycloakAdminTokenProvider adminTokenProvider;
  private final RestClient restClient;

  @SuppressWarnings("unchecked")
  public Map<String, Object> getRealmRoleAsMap(UserRole userRole) {
    String roleName = userRole.name().toLowerCase();
    String url = String.format("%s/admin/realms/%s/roles/%s",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), roleName);

    return restClient.get()
        .uri(url)
        .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
        .retrieve()
        .body(Map.class);
  }
}
