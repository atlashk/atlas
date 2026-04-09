package org.atlas.services.user.infrastructure.idp.client;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.user.domain.entity.UserEntity;
import org.atlas.services.user.infrastructure.idp.config.KeycloakProps;
import org.atlas.services.user.infrastructure.idp.enums.KeycloakUserAttribute;
import org.atlas.services.user.infrastructure.idp.exception.KeycloakClientException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

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
                String.format("Failed to create Keycloak user: userId=%s, status=%d",
                    user.getId(), response.getStatusCode().value()));
          })
          .toBodilessEntity()
          .getHeaders()
          .getFirst("Location");

      // Extract user ID from Location header
      String kcUserId = extractKcUserIdFromLocation(locationHeader);

      // Assign role
      assignUserRole(kcUserId, user.getRole());

      log.info("Created Keycloak user successfully: userId={}, kcUserId={}",
          user.getId(), kcUserId);
      return kcUserId;
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to create Keycloak user: userId=%s, reason=%s",
              user.getId(), e.getMessage()));
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

  public boolean existsByEmail(String email) {
    String url = UriComponentsBuilder
        .fromUriString(keycloakProps.getBaseUrl())
        .path("/admin/realms/{realm}/users")
        .queryParam("email", email)
        .queryParam("exact", true)
        .buildAndExpand(keycloakProps.getRealm())
        .toUriString();

    try {
      List<?> users = restClient.get()
          .uri(url)
          .headers(keycloakClientHelper.buildHeaders())
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to check Keycloak user by email: email=%s, status=%d",
                    email, response.getStatusCode().value()));
          })
          .body(List.class);

      return CollectionUtil.isNotEmpty(users);
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to check Keycloak user by email: email=%s, reason=%s",
              email, e.getMessage()));
    }
  }

  private Map<String, Object> buildUserPayload(UserEntity user) {
    Map<String, Object> payload = new HashMap<>();
    payload.put("username", user.getId());
    payload.put("firstName", StringUtil.defaultIfBlank(user.getFirstName(), StringUtil.EMPTY));
    payload.put("lastName", StringUtil.defaultIfBlank(user.getLastName(), StringUtil.EMPTY));
    payload.put("email", StringUtil.defaultIfBlank(user.getEmail(), StringUtil.EMPTY));
    payload.put("enabled", true);
    payload.put("attributes", Map.of(
        KeycloakUserAttribute.PHONE_NUMBER.getName(),
        List.of(StringUtil.defaultIfBlank(user.getPhoneNumber(), StringUtil.EMPTY))
    ));
    return payload;
  }

  private Map<String, Object> buildUserPayload(UserEntity user, String password) {
    Map<String, Object> payload = buildUserPayload(user);

    // Password
    payload.put("credentials", List.of(Map.of(
        "type", "password",
        "value", password,
        "temporary", false
    )));

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
