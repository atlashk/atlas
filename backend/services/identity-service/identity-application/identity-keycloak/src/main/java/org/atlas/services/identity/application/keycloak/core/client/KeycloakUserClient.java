package org.atlas.services.identity.application.keycloak.core.client;

import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.ExceptionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.application.keycloak.core.exception.KeycloakClientException;
import org.atlas.services.identity.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.identity.application.keycloak.core.util.KeycloakUtil;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.user")
public class KeycloakUserClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final KeycloakRealmRoleClient keycloakRealmRoleClient;

  public List<UserEntity> retrieveUserList(RetrieveUserListRequest request) {
    log.info("Retrieving Keycloak user list with criteria: userId={}, username={}, firstName={}, lastName={}, email={}",
        request.getUserId(), request.getUsername(), request.getFirstName(),
        request.getLastName(), request.getEmail());
    UsersResource usersResource = getUsersResource();
    log.info("Obtained Keycloak users resource: {}", usersResource);

    if (StringUtil.isNotBlank(request.getUserId())) {
      log.info("Searching Keycloak user by exact user ID: {}", request.getUserId());
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
      log.info("Searching Keycloak user list by criteria: username={}, firstName={}, lastName={}, email={}",
          request.getUsername(), request.getFirstName(), request.getLastName(), request.getEmail());
      try {
        // Paging parameters
        int first = request.getPagingRequest() == null ? 0 : request.getPagingRequest().getOffset();
        int max = request.getPagingRequest() == null ? 1 : request.getPagingRequest().getLimit();
        log.info("Retrieving Keycloak user list with paging: first={}, max={}", first, max);

        List<UserRepresentation> kcUsers = usersResource.list(
//            request.getUsername(), request.getFirstName(), 
//            request.getLastName(), request.getEmail(), 
            first, max);
        return MapperUtil.mapList(kcUsers, KeycloakUtil::toUserEntity);
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

    UsersResource usersResource = getUsersResource();
    List<UserEntity> userList = new ArrayList<>();
    for (String userId : userIds) {
      try {
        UserRepresentation kcUser = usersResource.get(userId).toRepresentation();
        UserEntity user = KeycloakUtil.toUserEntity(kcUser);
        userList.add(user);
      } catch (Exception e) {
        log.debug("Keycloak user {} not found: {}", userId, ExceptionUtil.getStacktrace(e));
      }
    }
    return userList;
  }

  public Optional<UserEntity> retrieveUser(String userId) {
    UsersResource usersResource = getUsersResource();
    try {
      UserRepresentation kcUser = usersResource.get(userId).toRepresentation();
      return Optional.of(KeycloakUtil.toUserEntity(kcUser));
    } catch (Exception e) {
      log.debug("Keycloak user {} not found: {}", userId, ExceptionUtil.getStacktrace(e));
      return Optional.empty();
    }
  }

  public Long retrieveTotalUserCount() {
    UsersResource usersResource = getUsersResource();
    return (long) usersResource.count();
  }

  public boolean existsByUsername(String username) {
    RetrieveUserListRequest retrieveUserListRequest = RetrieveUserListRequest.builder()
        .username(username)
        .build();
    return CollectionUtil.isNotEmpty(retrieveUserList(retrieveUserListRequest));
  }

  public boolean existsByEmail(String email) {
    RetrieveUserListRequest retrieveUserListRequest = RetrieveUserListRequest.builder()
        .email(email)
        .build();
    return CollectionUtil.isNotEmpty(retrieveUserList(retrieveUserListRequest));
  }

  public boolean existsByAttribute(KeycloakUserAttribute attribute, String value) {
    try {
      RealmResource realm = keycloak.realm(keycloakProps.getRealm());
      UsersResource users = realm.users();
      String query = String.format("attribute:%s", value);
      return CollectionUtil.isNotEmpty(users.searchByAttributes(query));
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
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    UsersResource usersResource = realm.users();
    UserRepresentation kcUser = toUserRepresentation(user, password);
    try (Response response = usersResource.create(kcUser)) {
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new KeycloakClientException(
            String.format("Failed to create Keycloak user: username=%s, status=%d, reason=%s",
                user.getUsername(), response.getStatus(),
                response.getStatusInfo().getReasonPhrase()));
      }

      // Assign role
      String kcCreatedId = CreatedResponseUtil.getCreatedId(response);
      UserResource userResource = usersResource.get(kcCreatedId);
      assignUserRole(userResource, user.getRole());

      log.info("Created Keycloak user successfully: username={}, keycloakUserId={}",
          user.getUsername(), kcCreatedId);
      return kcCreatedId;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to create Keycloak user: username=%s, reason=%s",
              user.getUsername(), e.getMessage()));
    }
  }

  public void updateUser(UserEntity user) {
    UsersResource usersResource = getUsersResource();
    try {
      // Update user info
      UserResource userResource = usersResource.get(user.getId());
      UserRepresentation kcUser = toUserRepresentation(user);
      userResource.update(kcUser);

      // Assign role
      assignUserRole(userResource, user.getRole());

      log.info("Updated Keycloak user successfully: id={}", user.getId());
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to update Keycloak user: username=%s, reason=%s",
              user.getUsername(), e.getMessage()));
    }
  }

  public void deleteUser(String userId) {
    UsersResource usersResource = getUsersResource();
    try (Response response = usersResource.delete(userId)) {
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new KeycloakClientException(
            String.format("Failed to delete Keycloak user: userId=%s, status=%d, reason=%s",
                userId, response.getStatus(), response.getStatusInfo().getReasonPhrase()));
      }
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to delete Keycloak user: userId=%s, reason=%s",
              userId, e.getMessage()));
    }
  }

  private UsersResource getUsersResource() {
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    log.info("Obtained Keycloak realm resource for realm '{}'", keycloakProps.getRealm());
    return realm.users();
  }

  private UserRepresentation toUserRepresentation(UserEntity user, String password) {
    // Basic info
    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setUsername(user.getUsername());
    kcUser.setFirstName(user.getFirstName());
    kcUser.setLastName(user.getLastName());
    kcUser.setEmail(user.getEmail());
    kcUser.setEnabled(true);

    // Password
    CredentialRepresentation kcCredential = toCredentialRepresentation(password);
    kcUser.setCredentials(Collections.singletonList(kcCredential));

    // Attributes
    kcUser.singleAttribute(KeycloakUserAttribute.PHONE_NUMBER.getName(), user.getPhoneNumber());

    return kcUser;
  }

  private UserRepresentation toUserRepresentation(UserEntity user) {
    UserRepresentation kcUser = new UserRepresentation();
    kcUser.setUsername(user.getUsername());
    kcUser.setFirstName(user.getFirstName());
    kcUser.setLastName(user.getLastName());
    kcUser.setEmail(user.getEmail());
    kcUser.setEnabled(true);
    kcUser.singleAttribute(KeycloakUserAttribute.PHONE_NUMBER.getName(), user.getPhoneNumber());
    return kcUser;
  }

  private CredentialRepresentation toCredentialRepresentation(String password) {
    CredentialRepresentation kcCredential = new CredentialRepresentation();
    kcCredential.setType(CredentialRepresentation.PASSWORD);
    kcCredential.setValue(password);
    kcCredential.setTemporary(Boolean.FALSE);
    return kcCredential;
  }

  private void assignUserRole(UserResource userResource, UserRole userRole) {
    List<RoleRepresentation> assignedKcRoles = userResource.roles().realmLevel().listAll();

    RoleRepresentation targetKcRole = keycloakRealmRoleClient.getRealmRole(userRole);

    // Remove other roles
    List<RoleRepresentation> kcRolesToRemove = assignedKcRoles.stream()
        .filter(kcRole -> !kcRole.getName().equals(targetKcRole.getName())).toList();
    if (!kcRolesToRemove.isEmpty()) {
      userResource.roles().realmLevel().remove(kcRolesToRemove);
    }

    // Assign target role if not already assigned
    boolean alreadyHasRole = assignedKcRoles.stream()
        .anyMatch(r -> r.getName().equals(targetKcRole.getName()));
    if (!alreadyHasRole) {
      userResource.roles().realmLevel().add(List.of(targetKcRole));
    }
  }
}
