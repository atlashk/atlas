package org.atlas.services.identity.application.keycloak.core.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.application.keycloak.core.exception.KeycloakClientException;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.user")
public class KeycloakUserClient {

  private final KeycloakProps keycloakProps;
  private final KeycloakClientHelper keycloakClientHelper;
  private final KeycloakRealmRoleClient keycloakRealmRoleClient;
  private final RestClient restClient;

  /**
   * @return Keycloak created user ID
   */
  public String createUser(UserEntity user, String password) {
    String url = String.format("%s/admin/realms/%s/users",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());

    Map<String, Object> userPayload = buildUserPayload(user, password);

    try {
      String locationHeader = restClient.post()
          .uri(url)
          .headers(keycloakClientHelper.buildHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .body(userPayload)
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to create Keycloak user: username=%s, status=%d",
                    user.getUsername(), response.getStatusCode().value()));
          })
          .toBodilessEntity()
          .getHeaders()
          .getFirst("Location");

      // Extract user ID from Location header
      String kcUserId = extractKcUserIdFromLocation(locationHeader);

      // Assign role
      assignUserRole(kcUserId, user.getRole());

      log.info("Created Keycloak user successfully: username={}, userId={}", 
          user.getUsername(), kcUserId);
      return kcUserId;
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to create Keycloak user: username=%s, reason=%s", 
              user.getUsername(), e.getMessage()));
    }
  }

  public void updateUser(UserEntity user) {
    String url = String.format("%s/admin/realms/%s/users/%s",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), user.getId());

    Map<String, Object> userPayload = buildUserPayload(user);

    try {
      restClient.put()
          .uri(url)
          .headers(keycloakClientHelper.buildHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .body(userPayload)
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to update Keycloak user: userId=%s, status=%d",
                    user.getId(), response.getStatusCode().value()));
          })
          .toBodilessEntity();

      // Assign role
      assignUserRole(user.getId(), user.getRole());

      log.info("Updated Keycloak user successfully: userId={}", user.getId());
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to update Keycloak user: userId=%s, reason=%s",
              user.getId(), e.getMessage()));
    }
  }

  public void deleteUser(String userId) {
    String url = String.format("%s/admin/realms/%s/users/%s",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    try {
      restClient.delete()
          .uri(url)
          .headers(keycloakClientHelper.buildHeaders())
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to delete Keycloak user: userId=%s, status=%d",
                    userId, response.getStatusCode().value()));
          })
          .toBodilessEntity();

      log.info("Deleted Keycloak user successfully: userId={}", userId);
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to delete Keycloak user: userId=%s, reason=%s",
              userId, e.getMessage()));
    }
  }

  private Map<String, Object> buildUserPayload(UserEntity user, String password) {
    Map<String, Object> payload = new HashMap<>(buildUserPayload(user));
    
    // Password
    payload.put("credentials", List.of(Map.of(
        "type", "password",
        "value", password,
        "temporary", false
    )));

    return payload;
  }

  private Map<String, Object> buildUserPayload(UserEntity user) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("username", user.getUsername());
    payload.put("firstName", user.getFirstName() != null ? user.getFirstName() : "");
    payload.put("lastName", user.getLastName() != null ? user.getLastName() : "");
    payload.put("email", user.getEmail() != null ? user.getEmail() : "");
    payload.put("enabled", true);
    payload.put("attributes", Map.of(
        KeycloakUserAttribute.PHONE_NUMBER.getName(),
        List.of(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
    ));
    return payload;
  }

  private String extractKcUserIdFromLocation(String locationHeader) {
    if (locationHeader == null) {
      throw new KeycloakClientException("Location header is missing");
    }
    return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
  }

  private void assignUserRole(String userId, UserRole userRole) {
    String roleName = userRole.name().toLowerCase();
    List<Map<String, Object>> assignedRoles = 
        keycloakRealmRoleClient.getUserAssignedRealmRoles(userId);

    // Check if already has the target role
    boolean alreadyHasRole = assignedRoles != null && assignedRoles.stream()
        .anyMatch(role -> roleName.equals(role.get("name")));

    if (alreadyHasRole) {
      log.info("Keycloak user {} already has role {}", userId, roleName);
      return;
    }

    List<Map<String, Object>> availableRoles = 
        keycloakRealmRoleClient.getUserAvailableRealmRoles(userId);

    // Find target role from available roles
    Map<String, Object> targetRole = availableRoles == null ? null : availableRoles.stream()
        .filter(role -> roleName.equals(role.get("name")))
        .findFirst()
        .orElse(null);

    if (targetRole == null) {
      log.warn("Role {} not found in available roles for keycloak user {}", roleName, userId);
      return;
    }

    // Remove other custom roles (not default-roles)
    if (assignedRoles != null && !assignedRoles.isEmpty()) {
      List<Map<String, Object>> rolesToRemove = assignedRoles.stream()
          .filter(role -> {
            String name = (String) role.get("name");
            return !roleName.equals(name) && !name.startsWith("default-roles");
          })
          .toList();

      if (!rolesToRemove.isEmpty()) {
        keycloakRealmRoleClient.removeUserRealmRoles(userId, rolesToRemove);
      }
    }

    // Assign target role
    keycloakRealmRoleClient.addUserRealmRoles(userId, List.of(targetRole));

    log.info("Assigned role {} to keycloak user {}", roleName, userId);
  }
}
