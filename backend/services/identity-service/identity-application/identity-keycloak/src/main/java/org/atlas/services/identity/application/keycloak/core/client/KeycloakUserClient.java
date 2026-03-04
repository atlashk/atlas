package org.atlas.services.identity.application.keycloak.core.client;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.application.keycloak.core.exception.KeycloakClientException;
import org.atlas.services.identity.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@SuppressWarnings("unchecked")
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.user")
public class KeycloakUserClient {

  private final KeycloakProps keycloakProps;
  private final KeycloakAdminTokenProvider adminTokenProvider;
  private final RestClient restClient;

  private static final ParameterizedTypeReference<List<Map<String, Object>>> USER_LIST_TYPE =
      new ParameterizedTypeReference<>() {};

  public List<UserEntity> retrieveUserList(RetrieveUserListRequest request) {
    if (StringUtil.isNotBlank(request.getUserId())) {
      // Search by exact user ID
      Optional<UserEntity> userOpt = retrieveUser(request.getUserId());
      if (userOpt.isEmpty()) {
        return CollectionUtil.emptyList();
      }

      // Verify other criteria match
      UserEntity user = userOpt.get();
      if ((StringUtil.isNotBlank(request.getUsername()) && !request.getUsername().equals(user.getUsername())) ||
          (StringUtil.isNotBlank(request.getFirstName()) && !request.getFirstName().equals(user.getFirstName())) ||
          (StringUtil.isNotBlank(request.getLastName()) && !request.getLastName().equals(user.getLastName())) ||
          (StringUtil.isNotBlank(request.getEmail()) && !request.getEmail().equals(user.getEmail()))) {
        return CollectionUtil.emptyList();
      }

      return Collections.singletonList(user);
    } else {
      // Search by username, first name, last name, and email
      try {
        int first = request.getPagingRequest() == null ? 0 : request.getPagingRequest().getOffset();
        int max = request.getPagingRequest() == null ? 1 : request.getPagingRequest().getLimit();

        String url = buildSearchUsersUrl(request.getUsername(), request.getFirstName(),
            request.getLastName(), request.getEmail(), first, max);

        List<Map<String, Object>> kcUsers = restClient.get()
            .uri(url)
            .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
            .retrieve()
            .body(USER_LIST_TYPE);

        return kcUsers == null ? Collections.emptyList() :
            kcUsers.stream().map(this::toUserEntity).toList();
      } catch (Exception e) {
        log.error("Failed to retrieve Keycloak user list: reason={}", e.getMessage());
        return Collections.emptyList();
      }
    }
  }

  public List<UserEntity> retrieveUserList(List<String> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return CollectionUtil.emptyList();
    }

    List<UserEntity> userList = new ArrayList<>();
    for (String userId : userIds) {
      retrieveUser(userId).ifPresent(userList::add);
    }
    return userList;
  }

  public Optional<UserEntity> retrieveUser(String userId) {
    String url = String.format("%s/admin/realms/%s/users/%s",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);
    try {
      Map<String, Object> kcUser = restClient.get()
          .uri(url)
          .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
          .retrieve()
          .onStatus(HttpStatusCode::is4xxClientError, (request, response) -> {
            throw new KeycloakClientException("User not found: " + userId);
          })
          .body(Map.class);
      return Optional.ofNullable(kcUser).map(this::toUserEntity);
    } catch (Exception e) {
      log.debug("Keycloak user {} not found: {}", userId, e.getMessage());
      return Optional.empty();
    }
  }

  public Long retrieveTotalUserCount() {
    String url = String.format("%s/admin/realms/%s/users/count",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    try {
      Integer count = restClient.get()
          .uri(url)
          .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
          .retrieve()
          .body(Integer.class);
      return count == null ? 0L : count.longValue();
    } catch (Exception e) {
      log.error("Failed to retrieve user count: {}", e.getMessage());
      return 0L;
    }
  }

  public boolean existsByUsername(String username) {
    RetrieveUserListRequest request = RetrieveUserListRequest.builder()
        .username(username)
        .build();
    return CollectionUtil.isNotEmpty(retrieveUserList(request));
  }

  public boolean existsByEmail(String email) {
    RetrieveUserListRequest request = RetrieveUserListRequest.builder()
        .email(email)
        .build();
    return CollectionUtil.isNotEmpty(retrieveUserList(request));
  }

  public boolean existsByAttribute(KeycloakUserAttribute attribute, String value) {
    try {
      String url = String.format("%s/admin/realms/%s/users?q=%s:%s",
          keycloakProps.getBaseUrl(), keycloakProps.getRealm(),
          attribute.getName(), value);

      List<Map<String, Object>> users = restClient.get()
          .uri(url)
          .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
          .retrieve()
          .body(USER_LIST_TYPE);

      return CollectionUtil.isNotEmpty(users);
    } catch (Exception e) {
      log.error("Failed to check exists by attribute: attribute={}, reason={}",
          attribute, e.getMessage());
      return false;
    }
  }

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
          .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
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
      String kcCreatedId = extractUserIdFromLocation(locationHeader);

      // Assign role
      assignUserRole(kcCreatedId, user.getRole());

      log.info("Created Keycloak user successfully: username={}, keycloakUserId={}",
          user.getUsername(), kcCreatedId);
      return kcCreatedId;
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
          .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
          .contentType(MediaType.APPLICATION_JSON)
          .body(userPayload)
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to update Keycloak user: username=%s, status=%d",
                    user.getUsername(), response.getStatusCode().value()));
          })
          .toBodilessEntity();

      // Assign role
      assignUserRole(user.getId(), user.getRole());

      log.info("Updated Keycloak user successfully: id={}", user.getId());
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to update Keycloak user: username=%s, reason=%s",
              user.getUsername(), e.getMessage()));
    }
  }

  public void deleteUser(String userId) {
    String url = String.format("%s/admin/realms/%s/users/%s",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    try {
      restClient.delete()
          .uri(url)
          .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
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

  private String buildSearchUsersUrl(String username, String firstName, String lastName,
                                      String email, int first, int max) {
    StringBuilder url = new StringBuilder(String.format("%s/admin/realms/%s/users?first=%d&max=%d",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), first, max));

    if (StringUtil.isNotBlank(username)) {
      url.append("&username=").append(username);
    }
    if (StringUtil.isNotBlank(firstName)) {
      url.append("&firstName=").append(firstName);
    }
    if (StringUtil.isNotBlank(lastName)) {
      url.append("&lastName=").append(lastName);
    }
    if (StringUtil.isNotBlank(email)) {
      url.append("&email=").append(email);
    }

    return url.toString();
  }

  private UserEntity toUserEntity(Map<String, Object> kcUser) {
    UserEntity user = new UserEntity();
    user.setId((String) kcUser.get("id"));
    user.setUsername((String) kcUser.get("username"));
    user.setFirstName((String) kcUser.get("firstName"));
    user.setLastName((String) kcUser.get("lastName"));
    user.setEmail((String) kcUser.get("email"));

    // Extract phone number from attributes
    Map<String, List<String>> attributes = (Map<String, List<String>>) kcUser.get("attributes");
    if (attributes != null) {
      List<String> phoneNumbers = attributes.get(KeycloakUserAttribute.PHONE_NUMBER.getName());
      if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
        user.setPhoneNumber(phoneNumbers.get(0));
      }
    }

    return user;
  }

  private Map<String, Object> buildUserPayload(UserEntity user, String password) {
    Map<String, Object> userPayload = new HashMap<>(buildUserPayload(user));
    userPayload.put("credentials", List.of(Map.of(
        "type", "password",
        "value", password,
        "temporary", false
    )));
    return userPayload;
  }

  private Map<String, Object> buildUserPayload(UserEntity user) {
    return Map.of(
        "username", user.getUsername(),
        "firstName", user.getFirstName() != null ? user.getFirstName() : "",
        "lastName", user.getLastName() != null ? user.getLastName() : "",
        "email", user.getEmail() != null ? user.getEmail() : "",
        "enabled", true,
        "attributes", Map.of(
            KeycloakUserAttribute.PHONE_NUMBER.getName(),
            List.of(user.getPhoneNumber() != null ? user.getPhoneNumber() : "")
        )
    );
  }

  private String extractUserIdFromLocation(String locationHeader) {
    if (locationHeader == null) {
      throw new KeycloakClientException("Location header is missing");
    }
    return locationHeader.substring(locationHeader.lastIndexOf("/") + 1);
  }

  private void assignUserRole(String userId, UserRole userRole) {
    String roleName = userRole.name().toLowerCase();
    
    // Get current roles
    String getRolesUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    List<Map<String, Object>> assignedRoles = restClient.get()
        .uri(getRolesUrl)
        .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    // Check if already has the target role
    boolean alreadyHasRole = assignedRoles != null && assignedRoles.stream()
        .anyMatch(role -> roleName.equals(role.get("name")));

    if (alreadyHasRole) {
      log.debug("User {} already has role {}", userId, roleName);
      return;
    }

    // Get available roles for user (this API only requires manage-users permission)
    String availableRolesUrl = String.format("%s/admin/realms/%s/users/%s/role-mappings/realm/available",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    List<Map<String, Object>> availableRoles = restClient.get()
        .uri(availableRolesUrl)
        .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
        .retrieve()
        .body(new ParameterizedTypeReference<>() {});

    // Find target role from available roles
    Map<String, Object> targetRole = availableRoles == null ? null : availableRoles.stream()
        .filter(role -> roleName.equals(role.get("name")))
        .findFirst()
        .orElse(null);

    if (targetRole == null) {
      log.warn("Role {} not found in available roles for user {}", roleName, userId);
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
        restClient.method(HttpMethod.DELETE)
            .uri(getRolesUrl)
            .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
            .contentType(MediaType.APPLICATION_JSON)
            .body(rolesToRemove)
            .retrieve()
            .toBodilessEntity();
      }
    }

    // Assign target role
    restClient.post()
        .uri(getRolesUrl)
        .header("Authorization", "Bearer " + adminTokenProvider.getAccessToken())
        .contentType(MediaType.APPLICATION_JSON)
        .body(List.of(targetRole))
        .retrieve()
        .toBodilessEntity();

    log.debug("Assigned role {} to user {}", roleName, userId);
  }
}
