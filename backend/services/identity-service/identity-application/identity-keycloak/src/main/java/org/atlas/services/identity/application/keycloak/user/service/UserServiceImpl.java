package org.atlas.services.identity.application.keycloak.user.service;

import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.error.CommonDomainError;
import org.atlas.libs.framework.domain.exception.BaseDomainException;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.core.enums.KeycloakUserAttribute;
import org.atlas.services.identity.application.keycloak.user.mapper.UserMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.user.model.ProfileOutput;
import org.atlas.services.identity.port.in.user.model.RegisterInput;
import org.atlas.services.identity.port.in.user.service.UserService;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

  private final KeycloakUserClient keycloakUserClient;

  @Override
  public void register(RegisterInput input) {
    checkValidity(input);

    UserEntity user = UserMapper.INSTANCE.toUser(input);
    user.setRole(UserRole.USER);
    keycloakUserClient.createUser(user, input.getPassword());
  }

  @Override
  public ProfileOutput retrieveProfile() {
    String userId = Contexts.getUserId();
    return keycloakUserClient.retrieveUser(userId)
        .map(UserMapper.INSTANCE::toProfileOutput)
        .orElseThrow(() -> new BaseDomainException(CommonDomainError.USER_NOT_FOUND));
  }

  private void checkValidity(RegisterInput input) {
    if (keycloakUserClient.existsByUsername(input.getUsername())) {
      throw new BaseDomainException(CommonDomainError.USERNAME_ALREADY_EXISTS);
    }
    if (keycloakUserClient.existsByEmail(input.getEmail())) {
      throw new BaseDomainException(CommonDomainError.EMAIL_ALREADY_EXISTS);
    }
    if (keycloakUserClient.existsByAttribute(KeycloakUserAttribute.PHONE_NUMBER,
        input.getPhoneNumber())) {
      throw new BaseDomainException(CommonDomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
