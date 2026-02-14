package org.atlas.services.iam.application.keycloak.front.service;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakAuthenticationClient;
import org.atlas.services.iam.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.iam.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.iam.application.keycloak.event.service.UserEventService;
import org.atlas.services.iam.application.keycloak.front.mapper.UserMapper;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;
import org.atlas.services.iam.port.in.front.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final KeycloakUserClient keycloakUserClient;
  private final KeycloakAuthenticationClient keycloakAuthenticationClient;
  private final UserEventService userEventService;

  @Override
  public void register(RegisterInput input) {
    checkValidity(input);

    UserEntity user = UserMapper.INSTANCE.toUser(input);
    user.setRole(UserRole.USER);
    keycloakUserClient.createUser(user, input.getPassword());

    userEventService.publishUserCreatedEvent(user);
  }

  @Override
  public ProfileOutput retrieveProfile() {
    String userId = Contexts.getUserId();
    return keycloakUserClient.retrieveUser(userId)
        .map(UserMapper.INSTANCE::toProfileOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  private void checkValidity(RegisterInput input) {
    if (keycloakUserClient.existsByUsername(input.getUsername())) {
      throw new DomainException(DomainError.USERNAME_ALREADY_EXISTS);
    }
    if (keycloakUserClient.existsByEmail(input.getEmail())) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }
    if (keycloakUserClient.existsByAttribute(KeycloakUserAttribute.PHONE_NUMBER,
        input.getPhoneNumber())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
