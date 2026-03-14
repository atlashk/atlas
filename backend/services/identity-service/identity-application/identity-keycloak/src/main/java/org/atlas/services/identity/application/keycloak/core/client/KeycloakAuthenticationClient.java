package org.atlas.services.identity.application.keycloak.core.client;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.framework.security.OAuth2Constant;
import org.atlas.libs.jwt.JwtUtil;
import org.atlas.services.identity.application.keycloak.core.config.KeycloakProps;
import org.atlas.services.identity.application.keycloak.core.exception.KeycloakClientException;
import org.atlas.services.identity.application.keycloak.core.model.TokenResponse;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
@Slf4j(topic = "keycloak.client.authentication")
public class KeycloakAuthenticationClient {

  private final KeycloakProps keycloakProps;
  private final KeycloakClientHelper keycloakClientHelper;
  private final RestClient restClient;

  public TokenResponse login(String email, String password) {
    String url = String.format("%s/realms/%s/protocol/openid-connect/token",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm());
    MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
    form.add("grant_type", OAuth2Constant.GRANT_TYPE_PASSWORD);
    form.add("client_id", keycloakProps.getClientId());
    form.add("client_secret", keycloakProps.getClientSecret());
    form.add("username", email);
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
    form.add("grant_type", OAuth2Constant.GRANT_TYPE_REFRESH_TOKEN);
    form.add("client_id", keycloakProps.getClientId());
    form.add("client_secret", keycloakProps.getClientSecret());
    form.add("refresh_token", refreshToken);
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

  public void logout(String accessToken) {
    try {
      String userId = JwtUtil.extractSubject(accessToken);
      String url = String.format("%s/admin/realms/%s/users/%s/logout",
          keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

      restClient.post()
          .uri(url)
          .headers(keycloakClientHelper.buildHeaders())
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to logout user: userId=%s, status=%d",
                    userId, response.getStatusCode().value()));
          })
          .toBodilessEntity();

      log.info("Logged out Keycloak user successfully: userId={}", userId);
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException("Failed to logout user: " + e.getMessage());
    }
  }

  public void changePassword(String userId, String newPassword) {
    String url = String.format("%s/admin/realms/%s/users/%s/reset-password",
        keycloakProps.getBaseUrl(), keycloakProps.getRealm(), userId);

    Map<String, Object> credential = Map.of(
        "type", "password",
        "value", newPassword,
        "temporary", false
    );

    try {
      restClient.put()
          .uri(url)
          .headers(keycloakClientHelper.buildHeaders())
          .contentType(MediaType.APPLICATION_JSON)
          .body(credential)
          .retrieve()
          .onStatus(HttpStatusCode::isError, (request, response) -> {
            throw new KeycloakClientException(
                String.format("Failed to change password: userId=%s, status=%d",
                    userId, response.getStatusCode().value()));
          })
          .toBodilessEntity();

      log.info("Changed Keycloak user password successfully: userId={}", userId);
    } catch (KeycloakClientException e) {
      throw e;
    } catch (Exception e) {
      throw new KeycloakClientException(
          String.format("Failed to change Keycloak user password: id=%s, reason=%s",
              userId, e.getMessage()));
    }
  }
}

