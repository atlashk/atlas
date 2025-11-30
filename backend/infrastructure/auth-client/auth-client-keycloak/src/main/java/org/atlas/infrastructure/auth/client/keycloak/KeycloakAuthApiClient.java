package org.atlas.infrastructure.auth.client.keycloak;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.exception.AuthClientException;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
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
@Slf4j
public class KeycloakAuthApiClient implements AuthApiClient {

  private final Keycloak keycloakClient;
  private final KeycloakProps keycloakProps;

  @Override
  public void createUser(CreateUserRequest request) {
    log.info("Attempting to create user with username: {}", request.getUsername());

    Response response = null;
    try {
      RealmResource realm = keycloakClient.realm(keycloakProps.getRealmName());
      UsersResource usersResource = realm.users();

      // 1) Create user
      UserRepresentation userRepresentation = toUserRepresentation(request);
      response = usersResource.create(userRepresentation);

      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new AuthClientException(String.format(
            "Failed to create Keycloak user for username %s: HTTP %d %s",
            request.getUsername(),
            response.getStatus(),
            response.getStatusInfo().getReasonPhrase()
        ));
      }

      String kcUserId = CreatedResponseUtil.getCreatedId(response);
      UserResource userResource = usersResource.get(kcUserId);

      // 2) Set password
      CredentialRepresentation credential = toCredentialRepresentation(request);
      userResource.resetPassword(credential);

      // 3) Assign realm role (if provided)
      if (request.getRole() != null) {
        RoleRepresentation realmRole = realm.roles()
            .get(request.getRole().name())
            .toRepresentation();

        userResource.roles()
            .realmLevel()
            .add(Collections.singletonList(realmRole));
      }

      log.info("Created Keycloak user successfully for username {}", request.getUsername());
    } catch (Exception e) {
      throw new AuthClientException(String.format(
          "Failed to create Keycloak user for username '%s'",
          request.getUsername()), e);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  private UserRepresentation toUserRepresentation(CreateUserRequest request) {
    UserRepresentation user = new UserRepresentation();
    user.setUsername(request.getUsername());
    user.setEmail(request.getEmail());
    user.setEnabled(true);

    // Attach userId as a user attribute so it can be mapped to a token claim
    if (request.getUserId() != null) {
      user.singleAttribute("userId", String.valueOf(request.getUserId()));
    }

    return user;
  }

  private CredentialRepresentation toCredentialRepresentation(CreateUserRequest request) {
    CredentialRepresentation credential = new CredentialRepresentation();
    credential.setType(CredentialRepresentation.PASSWORD);
    credential.setValue(request.getPassword());
    credential.setTemporary(false);
    return credential;
  }
}
