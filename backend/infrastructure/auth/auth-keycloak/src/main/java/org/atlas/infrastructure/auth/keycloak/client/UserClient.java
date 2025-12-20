package org.atlas.infrastructure.auth.keycloak.client;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.collection.MapUtil;
import org.atlas.infrastructure.auth.keycloak.config.KeycloakProps;
import org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest;
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
@Slf4j
public class UserClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final RealmRoleClient realmRoleClient;

  public void createUser(CreateUserRequest request) {
    Response response = null;
    try {
      RealmResource realm = keycloak.realm(keycloakProps.getRealm());
      UsersResource users = realm.users();
      UserRepresentation user = toUserRepresentation(request);
      response = users.create(user);
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new RuntimeException(String.format(
            "Failed to create Keycloak user: username=%s, status=%d, reason=%s",
            request.getUsername(),
            response.getStatus(),
            response.getStatusInfo().getReasonPhrase()
        ));
      }
      String kcUserId = CreatedResponseUtil.getCreatedId(response);

      // Assign realm role
      RoleRepresentation realmRole = realmRoleClient.getRealmRole(request.getRole());
      users.get(kcUserId)
          .roles()
          .realmLevel()
          .add(List.of(realmRole));

      log.info("Created Keycloak user successfully: username={}, Keycloak userId={}",
          request.getUsername(), kcUserId);
    } catch (Exception e) {
      log.error("Failed to create Keycloak user: username={}, error={}",
          request.getUsername(), e.getMessage(), e);
      throw e;
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  private UserRepresentation toUserRepresentation(CreateUserRequest request) {
    // User information
    UserRepresentation user = new UserRepresentation();
    user.setUsername(request.getUsername());
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setEmail(request.getEmail());
    user.setEnabled(true);

    // Password
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(request.getPassword());
    credential.setTemporary(Boolean.FALSE);
    user.setCredentials(Collections.singletonList(credential));

    // Attributes
    if (MapUtil.isNotEmpty(request.getAttributes())) {
      request.getAttributes().forEach(user::singleAttribute);
    }

    return user;
  }
}
