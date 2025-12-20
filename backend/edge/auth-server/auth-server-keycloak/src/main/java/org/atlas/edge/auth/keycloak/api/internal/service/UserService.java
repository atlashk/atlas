package org.atlas.edge.auth.keycloak.api.internal.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.edge.auth.keycloak.api.internal.mapper.UserMapper;
import org.atlas.edge.auth.keycloak.api.internal.model.CreateUserRequest;
import org.atlas.infrastructure.auth.keycloak.client.UserClient;
import org.atlas.infrastructure.auth.keycloak.constant.Attributes;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserClient userClient;

  public void createUser(CreateUserRequest request) {
    org.atlas.infrastructure.auth.keycloak.model.CreateUserRequest keycloakRequest =
        UserMapper.INSTANCE.toKeycloakCreateUserRequest(request);
    keycloakRequest.setAttributes(Map.of(
        Attributes.USER_ID, String.valueOf(request.getUserId()),
        Attributes.PHONE_NUMBER, request.getPhoneNumber()
    ));
    userClient.createUser(keycloakRequest);
  }
}
