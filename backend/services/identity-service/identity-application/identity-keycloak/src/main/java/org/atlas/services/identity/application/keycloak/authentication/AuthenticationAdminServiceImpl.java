package org.atlas.services.identity.application.keycloak.authentication;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.security.authorization.RequiredAdmin;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakAuthenticationClient;
import org.atlas.services.identity.port.in.authentication.service.AuthenticationAdminService;
import org.springframework.stereotype.Service;

@Service
@RequiredAdmin
@RequiredArgsConstructor
public class AuthenticationAdminServiceImpl implements AuthenticationAdminService {

  private final ApplicationConfigService applicationConfigService;
  private final KeycloakAuthenticationClient keycloakAuthenticationClient;

  @Override
  public void resetPassword(String userId) throws Exception {
    String defaultPassword = Optional.ofNullable(
            applicationConfigService.getConfig("security.default-password"))
        .orElseThrow(() -> new RuntimeException("Default password not set"));
    keycloakAuthenticationClient.changePassword(userId, defaultPassword);
  }
}
