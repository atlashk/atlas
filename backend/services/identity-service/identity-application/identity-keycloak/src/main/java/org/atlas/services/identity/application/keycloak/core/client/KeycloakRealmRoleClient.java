package org.atlas.services.identity.application.keycloak.core.client;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.realm_role")
public class KeycloakRealmRoleClient {

  private final KeycloakProps keycloakProps;
  private final KeycloakClientHelper keycloakClientHelper;
  private final RestClient restClient;

  private static final ParameterizedTypeReference<List<Map<String, Object>>> ROLE_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  @SuppressWarnings("unchecked")
  public Map<String, Object> getRealmRoleAsMap(UserRole userRole) {
    String roleName = userRole.name().toLowerCase();
    String url = String.format("%s/admin/realms/%s/roles/%s",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), roleName);

    return restClient.get()
        .uri(url)
        .headers(keycloakClientHelper.buildHeaders())
        .retrieve()
        .body(Map.class);
  }

  public List<Map<String, Object>> getUserAssignedRealmRoles(String userId) {
    String url = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    List<Map<String, Object>> roles = restClient.get()
        .uri(url)
        .headers(keycloakClientHelper.buildHeaders())
        .retrieve()
        .body(ROLE_LIST_TYPE);

    return roles == null ? Collections.emptyList() : roles;
  }

  public List<Map<String, Object>> getUserAvailableRealmRoles(String userId) {
    String url = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm/available",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    List<Map<String, Object>> roles = restClient.get()
        .uri(url)
        .headers(keycloakClientHelper.buildHeaders())
        .retrieve()
        .body(ROLE_LIST_TYPE);

    return roles == null ? Collections.emptyList() : roles;
  }

  public void removeUserRealmRoles(String userId, List<Map<String, Object>> rolesToRemove) {
    if (CollectionUtil.isEmpty(rolesToRemove)) {
      return;
    }

    String url = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    restClient.method(HttpMethod.DELETE)
        .uri(url)
        .headers(keycloakClientHelper.buildHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .body(rolesToRemove)
        .retrieve()
        .toBodilessEntity();
  }

  public void addUserRealmRoles(String userId, List<Map<String, Object>> rolesToAdd) {
    if (CollectionUtil.isEmpty(rolesToAdd)) {
      return;
    }

    String url = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    restClient.post()
        .uri(url)
        .headers(keycloakClientHelper.buildHeaders())
        .contentType(MediaType.APPLICATION_JSON)
        .body(rolesToAdd)
        .retrieve()
        .toBodilessEntity();
  }

  public Optional<UserRole> resolveUserRole(String userId) {
    List<Map<String, Object>> assignedRoles = getUserAssignedRealmRoles(userId);
    if (CollectionUtil.isEmpty(assignedRoles)) {
      return Optional.empty();
    }

    for (Map<String, Object> role : assignedRoles) {
      String name = role == null ? null : (String) role.get("name");
      if (name == null || name.startsWith("default-roles")) {
        continue;
      }
      for (UserRole userRole : UserRole.values()) {
        if (userRole.name().equalsIgnoreCase(name)) {
          return Optional.of(userRole);
        }
      }
    }

    return Optional.empty();
  }
}
