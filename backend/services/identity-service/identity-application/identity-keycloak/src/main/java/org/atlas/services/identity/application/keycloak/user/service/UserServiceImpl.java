package org.atlas.services.identity.application.keycloak.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.services.identity.application.keycloak.core.client.KeycloakUserClient;
import org.atlas.services.identity.application.keycloak.user.mapper.UserAdminMapper;
import org.atlas.services.identity.application.keycloak.user.mapper.UserMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.domain.error.DomainError;
import org.atlas.services.identity.domain.exception.DomainException;
import org.atlas.services.identity.port.in.user.model.ProfileOutput;
import org.atlas.services.identity.port.in.user.model.RegisterInput;
import org.atlas.services.identity.port.in.user.service.UserService;
import org.atlas.services.identity.port.out.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final SequenceGenerator sequenceGenerator;
  private final KeycloakUserClient keycloakUserClient;

  @Override
  @Transactional
  public void register(RegisterInput input) {
    checkValidity(input);

    // 1. Save to DB first (without password - Keycloak handles password)
    UserEntity user = UserMapper.INSTANCE.toUser(input);
    user.setId(sequenceGenerator.generate(SequenceType.USER));
    user.setRole(UserRole.USER);
    userRepository.insert(user);

    // 2. Sync to Keycloak
    try {
      String kcUserId = keycloakUserClient.createUser(user, input.getPassword());
      
      // Update DB with Keycloak user ID for future reference
      user.setExternalId(kcUserId);
      userRepository.update(user);
    } catch (Exception e) {
      log.error("Failed to sync user to Keycloak: userId={}, error={}", 
          user.getId(), e.getMessage(), e);
      throw new DomainException(DomainError.USER_REGISTRATION_FAILED, e);
    }
  }

  @Override
  @Transactional(readOnly = true)
  public ProfileOutput retrieveProfile() {
    String userId = Contexts.getUserId();
    // Query from DB for better performance
    return userRepository.findById(userId)
        .map(UserMapper.INSTANCE::toProfileOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  private void checkValidity(RegisterInput input) {
    // Validate against DB (better query performance)
    if (userRepository.existsByUsername(input.getUsername())) {
      throw new DomainException(DomainError.USERNAME_ALREADY_EXISTS);
    }
    if (userRepository.existsByEmail(input.getEmail())) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.existsByPhoneNumber(input.getPhoneNumber())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
