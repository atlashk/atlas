package org.atlas.services.identity.application.keycloak.core.client;

import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.atlas.libs.framework.security.OAuth2Constant;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.admin_token")
public class KeycloakAdminTokenProvider {

  private final KeycloakProps keycloakProps;
  private final RestClient restClient;

  private String accessToken;
  private Instant tokenExpiry;

  public synchronized String getAccessToken() {
    if (accessToken == null || isTokenExpired()) {
      refreshAdminToken();
    }
    return accessToken;
  }

  private boolean isTokenExpired() {
    return tokenExpiry == null || Instant.now().isAfter(tokenExpiry.minusSeconds(30));
  }

  @SuppressWarnings("unchecked")
  private void refreshAdminToken() {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());

    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", OAuth2Constant.GRANT_TYPE_CLIENT_CREDENTIALS);
    form.add("client_id", keycloakProps.getClientId());
    form.add("client_secret", keycloakProps.getClientSecret());

    Map<String, Object> response = restClient.post()
        .uri(url)
        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
        .body(form)
        .retrieve()
        .body(Map.class);

    if (response != null) {
      this.accessToken = (String) response.get("access_token");
      Integer expiresIn = (Integer) response.get("expires_in");
      this.tokenExpiry = Instant.now().plusSeconds(expiresIn != null ? expiresIn : 300);
      log.debug("Refreshed admin token, expires in {} seconds", expiresIn);
    }
  }
}
