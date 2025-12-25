package org.atlas.infrastructure.iam.keycloak.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.infrastructure.iam.keycloak.config.KeycloakProps;
import org.atlas.infrastructure.iam.keycloak.model.TokenResponse;
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

  private final KeycloakProps keycloakProps;
  private final RestClient restClient;

  public TokenResponse login(String username, String password) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "password");
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
          throw new DomainException(DomainError.UNAUTHORIZED);
        })
        .body(TokenResponse.class);
  }

  public TokenResponse refreshToken(String refreshToken) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", "refresh_token");
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

  public void revokeAccessToken(String accessToken) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/revoke",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("client_id", keycloakProps.getClientId());
    form.add("client_secret", keycloakProps.getClientSecret());
    form.add("token", accessToken);
    form.add("token_type_hint", "access_token");
    restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .toBodilessEntity();
  }
}
