package org.atlas.services.user.infrastructure.idp.client;

import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakClientHelper {

  private final KeycloakAdminTokenProvider keycloakAdminTokenProvider;

  public Consumer<HttpHeaders> buildHeaders() {
    return headers ->
        headers.setBearerAuth(keycloakAdminTokenProvider.getAccessToken());
  }
}
