package org.atlas.edge.auth.keycloak.api.internal.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.keycloak.api.internal.model.CreateUserRequest;
import org.atlas.infrastructure.auth.keycloak.client.KeycloakClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {

  private final KeycloakClient keycloakClient;

  public void createUser(CreateUserRequest request) {
    org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest keycloakRequest = org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest.builder()
        .username(request.getUsername())
        .password(request.getPassword())
        .firstName(request.getFirstName())
        .lastName(request.getLastName())
        .email(request.getEmail())
        .build();
    keycloakRequest.setAttributes(Map.of(
        "userId", String.valueOf(request.getUserId()),
        "phoneNumber", request.getPhoneNumber()
    ));
    keycloakClient.createUser(keycloakRequest);
  }
}
