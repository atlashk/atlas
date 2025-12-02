package org.atlas.infrastructure.auth.keycloak;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.framework.auth.AuthService;
import org.atlas.framework.auth.model.CreateUserRequest;
import org.atlas.infrastructure.auth.keycloak.client.KeycloakClient;
import org.atlas.infrastructure.auth.keycloak.mapper.KeycloakMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KeycloakAuthService implements AuthService {

  private final KeycloakClient keycloakClient;

  @Override
  public void createUser(CreateUserRequest request) {
    org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest keycloakCreateUserRequest =
        KeycloakMapper.INSTANCE.toKeycloakCreateUserRequest(request);
    keycloakCreateUserRequest.setAttributes(Map.of(
        "userId", String.valueOf(request.getUserId()),
        "phoneNumber", request.getPhoneNumber()
    ));
    keycloakClient.createUser(keycloakCreateUserRequest);
  }
}
