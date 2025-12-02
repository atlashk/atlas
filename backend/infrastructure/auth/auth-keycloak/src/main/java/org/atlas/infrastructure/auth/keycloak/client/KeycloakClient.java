package org.atlas.infrastructure.auth.keycloak.client;

import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.collection.CollectionUtil;
import org.atlas.framework.collection.MapUtil;
import org.atlas.infrastructure.auth.keycloak.config.KeycloakProps;
import org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest;
import org.atlas.infrastructure.auth.keycloak.model.TokenResponse;
import org.atlas.infrastructure.api.client.rest.resttemplate.RestTemplateService;
import org.springframework.http.HttpHeaders;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final RestTemplateService restTemplateService;

  public void createUser(CreateUserRequest request) {
    Response response = null;
    try {
      RealmResource realm = keycloak.realm(keycloakProps.getRealmName());
      UsersResource usersResource = realm.users();
      UserRepresentation userRepresentation = toUserRepresentation(request);
      response = usersResource.create(userRepresentation);
      if (response.getStatusInfo().getFamily() != Response.Status.Family.SUCCESSFUL) {
        throw new RuntimeException(String.format(
            "Failed to create Keycloak user: username=%s, status=%d, reason=%s",
            request.getUsername(),
            response.getStatus(),
            response.getStatusInfo().getReasonPhrase()
        ));
      }
      String kcUserId = CreatedResponseUtil.getCreatedId(response);
      log.info("Created Keycloak user successfully: username={}, Keycloak userId={}",
          request.getUsername(), kcUserId);
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

    // Roles
    user.setRealmRoles(Collections.singletonList(request.getRole().name()));

    // Attributes
    if (MapUtil.isNotEmpty(request.getAttributes())) {
      request.getAttributes().forEach(user::singleAttribute);
    }

    return user;
  }

  public TokenResponse login(String username, String password) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealmName());
    return restTemplateService.doPost(url,
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"),
        Map.of(
            "grant_type", "password",
            "client_id", keycloakProps.getClientId(),
            "client_secret", keycloakProps.getClientSecret(),
            "username", username,
            "password", password
        ), TokenResponse.class);
  }

  public TokenResponse refreshToken(String refreshToken) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealmName());
    return restTemplateService.doPost(url,
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"),
        Map.of(
            "grant_type", "refresh_token",
            "client_id", keycloakProps.getClientId(),
            "client_secret", keycloakProps.getClientSecret(),
            "refresh_token", refreshToken
        ), TokenResponse.class);
  }

  public void revokeAccessToken(String accessToken) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/revoke",
        keycloakProps.getBaseUrl(), keycloakProps.getRealmName());
    restTemplateService.doPost(url,
        Collections.singletonMap(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded"),
        Map.of(
            "client_id", keycloakProps.getClientId(),
            "client_secret", keycloakProps.getClientSecret(),
            "token", accessToken,
            "token_type_hint", "access_token"
        ), Void.class);
  }
}
