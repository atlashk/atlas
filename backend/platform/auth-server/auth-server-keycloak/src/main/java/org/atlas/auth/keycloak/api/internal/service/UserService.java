package org.atlas.auth.keycloak.api.internal.service;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.atlas.auth.keycloak.api.internal.mapper.UserMapper;
import org.atlas.auth.keycloak.api.internal.model.CreateUserRequest;
import org.atlas.common.infrastructure.iam.keycloak.client.KeycloakUserClient;
import org.atlas.common.infrastructure.iam.keycloak.constant.Attributes;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final KeycloakUserClient keycloakUserClient;

  public void createUser(CreateUserRequest request) {
    org.atlas.common.infrastructure.iam.keycloak.model.CreateUserRequest keycloakRequest =
        UserMapper.INSTANCE.toKeycloakCreateUserRequest(request);
    keycloakRequest.setAttributes(Map.of(
        Attributes.USER_ID, String.valueOf(request.getUserId()),
        Attributes.PHONE_NUMBER, request.getPhoneNumber()
    ));
    keycloakUserClient.createUser(keycloakRequest);
  }
}
