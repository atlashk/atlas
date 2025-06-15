package org.atlas.infrastructure.auth.client.keycloak;

import jakarta.ws.rs.core.Response;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.auth.client.AuthClientPort;
import org.atlas.framework.auth.client.exception.AuthClientException;
import org.atlas.framework.auth.client.model.CreateUserRequest;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthClientAdapter implements AuthClientPort {

  private final Keycloak keycloakClient;
  private final KeycloakProps keycloakProps;

  @Override
  public void createUser(CreateUserRequest request) {
    log.info("Attempting to create user with username: {}", request.getUsername());
    UserRepresentation userRepresentation = toUserRepresentation(request);

    try (Response response = keycloakClient.realm(keycloakProps.getRealmName())
        .users()
        .create(userRepresentation)) {
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new AuthClientException(
            String.format("Failed to create Keycloak user for username %s: HTTP %d %s",
                request.getUsername(), response.getStatus(),
                response.getStatusInfo().getReasonPhrase()));
      }
      log.info("Created Keycloak user successfully for username {}", request.getUsername());
    } catch (Exception e) {
      throw new AuthClientException(
          String.format("Failed to create Keycloak user for username %s: %s",
              request.getUsername(), e));
    }
  }

  private UserRepresentation toUserRepresentation(CreateUserRequest request) {
    UserRepresentation userRepresentation = new UserRepresentation();
    userRepresentation.setId(String.valueOf(request.getUserId()));
    userRepresentation.setUsername(request.getUsername());
    userRepresentation.setEmail(request.getEmail());
    userRepresentation.setEnabled(true);
    userRepresentation.setCredentials(List.of(toCredentialRepresentation(request)));
    return userRepresentation;
  }

  private CredentialRepresentation toCredentialRepresentation(CreateUserRequest request) {
    CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
    credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
    credentialRepresentation.setValue(request.getPassword());
    credentialRepresentation.setTemporary(false);
    return credentialRepresentation;
  }
}
