package org.atlas.services.identity.application.keycloak.core.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.jwt.JwtUtil;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.identity.application.keycloak.core.exception.KeycloakClientException;
import org.atlas.services.identity.application.keycloak.core.model.TokenResponse;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthenticationClient {

  private final Keycloak keycloak;
  private final KeycloakProps keycloakProps;
  private final RestClient restClient;

  public TokenResponse login(String username, String password) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", OAuth2Constants.PASSWORD);
    form.add("client_id", keycloakProps.getClientId());
    form.add("client_secret", keycloakProps.getClientSecret());
    form.add("username", username);
    form.add("password", password);
    return restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .onStatus(HttpStatusCode::isError, (request, response) -> {
          throw new BaseDomainException(CommonDomainError.UNAUTHORIZED);
        })
        .body(TokenResponse.class);
  }

  public TokenResponse refreshToken(String refreshToken) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", OAuth2Constants.REFRESH_TOKEN);
    form.add("client_id", keycloakProps.getClientId());
    form.add("client_secret", keycloakProps.getClientSecret());
    form.add("refresh_token", refreshToken);
    return restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .toEntity(TokenResponse.class)
        .getBody();
  }

  public void logout(String accessToken) throws Exception {
    String userId = JwtUtil.extractSubject(accessToken);
    UsersResource usersResource = getUsersResource();
    UserResource userResource = usersResource.get(userId);
    userResource.logout();
  }

  public void changePassword(String userId, String newPassword) {
    UsersResource usersResource = getUsersResource();
    try {
      UserResource userResource = usersResource.get(userId);
      CredentialRepresentation kcCredential = toCredentialRepresentation(newPassword);
      userResource.resetPassword(kcCredential);
      log.info("Changed Keycloak user password successfully: userId={}", userId);
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to change Keycloak user password: id=%s, reason=%s",
              userId, e.getMessage()));
    }
  }

  private UsersResource getUsersResource() {
    RealmResource realm = keycloak.realm(keycloakProps.getRealm());
    return realm.users();
  }

  private CredentialRepresentation toCredentialRepresentation(String password) {
    CredentialRepresentation kcCredential = new CredentialRepresentation();
    kcCredential.setType(CredentialRepresentation.PASSWORD);
    kcCredential.setValue(password);
    kcCredential.setTemporary(Boolean.FALSE);
    return kcCredential;
  }
}

