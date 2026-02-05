package org.atlas.services.iam.application.keycloak.core.client;

import jakarta.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.libs.framework.util.ExceptionUtil;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.iam.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.iam.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.iam.application.keycloak.core.model.RetrieveUserListRequest;
import org.atlas.services.iam.application.keycloak.core.util.KeycloakUtil;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak")
public class KeycloakUserClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final KeycloakRealmRoleClient keycloakRealmRoleClient;

  public List<UserEntity> retrieveUserList(RetrieveUserListRequest request) {

    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    UsersResource users = realm.users();

    if (StringUtil.isNotBlank(request.getUserId())) {
      // Search by exact user ID
      Optional<UserEntity> userOpt = retrieveUser(request.getUserId());
      if (userOpt.isEmpty()) {
        return CollectionUtil.emptyList();
      }

      // Verify other criteria match
      UserEntity user = userOpt.get();
      if ((StringUtil.isNotBlank(request.getUsername())
          && !request.getUsername().equals(user.getUsername())) ||
          (StringUtil.isNotBlank(request.getFirstName())
              && !request.getFirstName().equals(user.getFirstName())) ||
          (StringUtil.isNotBlank(request.getLastName())
              && !request.getLastName().equals(user.getLastName())) ||
          (StringUtil.isNotBlank(request.getEmail())
              && !request.getEmail().equals(user.getEmail()))) {
        return CollectionUtil.emptyList();
      }

      return Collections.singletonList(user);
    } else {
      // Search by username, first name, last name, and email
      try {
        // Paging parameters
        int first = request.getPagingRequest() == null ? 0 : request.getPagingRequest().getOffset();
        int max = request.getPagingRequest() == null ? 1 : request.getPagingRequest().getLimit();

        List<UserRepresentation> kcUsers = users.search(
            StringUtil.trimToEmpty(request.getUsername()),
            StringUtil.trimToEmpty(request.getFirstName()),
            StringUtil.trimToEmpty(request.getLastName()),
            StringUtil.trimToEmpty(request.getEmail()),
            first,
            max
        );
        return MapperUtil.mapList(kcUsers, KeycloakUtil::toUserEntity);
      } catch (Exception e) {
        log.error("Failed to search Keycloak users: request={}, error={}",
            request, e.getMessage(), e);
        throw e;
      }
    }
  }

  public List<UserEntity> retrieveUserList(List<String> userIds) {
    if (CollectionUtil.isEmpty(userIds)) {
      return CollectionUtil.emptyList();
    }

    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    UsersResource users = realm.users();
    List<UserEntity> userList = new ArrayList<>();
    for (String userId : userIds) {
      try {
        UserRepresentation kcUser = users.get(userId).toRepresentation();
        UserEntity user = KeycloakUtil.toUserEntity(kcUser);
        userList.add(user);
      } catch (Exception e) {
        log.debug("Keycloak user {} not found: {}", userId, ExceptionUtil.getStacktrace(e));
      }
    }
    return userList;
  }

  public Optional<UserEntity> retrieveUser(String userId) {
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    UsersResource users = realm.users();
    try {
      UserRepresentation kcUser = users.get(userId).toRepresentation();
      return Optional.of(KeycloakUtil.toUserEntity(kcUser));
    } catch (Exception e) {
      log.debug("Keycloak user {} not found: {}", userId, ExceptionUtil.getStacktrace(e));
      return Optional.empty();
    }
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
      log.error("Failed to check exists by attribute: attribute={}, value={}, error={}",
          attribute, value, e.getMessage(), e);
      throw e;
    }
  }

  public String createUser(UserEntity user, String password) {
    Response response = null;
    try {
      RealmResource realm = keycloak.realm(keycloakProps.getRealm());
      UsersResource users = realm.users();
      UserRepresentation kcUser = toUserRepresentation(user, password);
      response = users.create(kcUser);
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new RuntimeException(String.format(
            "Failed to create Keycloak user: username=%s, status=%d, reason=%s",
            user.getUsername(),
            response.getStatus(),
            response.getStatusInfo().getReasonPhrase()
        ));
      }
      String kcUserId = CreatedResponseUtil.getCreatedId(response);

      // Add role
      RoleRepresentation realmRole = keycloakRealmRoleClient.getRealmRole(user.getRole());
      users.get(kcUserId)
          .roles()
          .realmLevel()
          .add(List.of(realmRole));

      log.info("Created Keycloak user successfully: username={}, keycloakUserId={}",
          user.getUsername(), kcUserId);
      return kcUserId;
    } catch (Exception e) {
      log.error("Failed to create Keycloak user: username={}, error={}",
          user.getUsername(), e.getMessage(), e);
      throw e;
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  public void changePassword(String userId, String newPassword) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(newPassword);
    credential.setTemporary(Boolean.FALSE);
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    realm.users().get(userId).resetPassword(credential);
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

  private CredentialRepresentation toCredentialRepresentation(String password) {
    CredentialRepresentation kcCredential = new CredentialRepresentation();
    kcCredential.setType(CredentialRepresentation.PASSWORD);
    kcCredential.setValue(password);
    kcCredential.setTemporary(Boolean.FALSE);
    return kcCredential;
  }
}
