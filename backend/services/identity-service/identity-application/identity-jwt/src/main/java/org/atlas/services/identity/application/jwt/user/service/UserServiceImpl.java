package org.atlas.services.identity.application.jwt.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.identity.UserRole;
import org.atlas.services.identity.application.jwt.user.mapper.UserEventMapper;
import org.atlas.services.identity.application.jwt.user.mapper.UserMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.user.model.ProfileOutput;
import org.atlas.services.identity.port.in.user.model.RegisterInput;
import org.atlas.services.identity.port.in.user.service.UserService;
import org.atlas.services.identity.port.out.messaging.UserEventMessagePublisher;
import org.atlas.services.identity.port.out.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserEventMessagePublisher messagePublisher;

  @Override
  @Transactional(readOnly = true)
  public ProfileOutput retrieveProfile() {
    String userId = Contexts.getUserId();
    return userRepository.findById(userId)
        .map(UserMapper.INSTANCE::toProfileOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public void register(RegisterInput input) {
    checkValidity(input);

    UserEntity user = UserMapper.INSTANCE.toUser(input);
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    user.setRole(UserRole.USER);
    userRepository.insert(user);

    publishUserCreatedEvent(user);
  }

  private void checkValidity(RegisterInput input) {
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

  private void publishUserCreatedEvent(UserEntity user) {
    UserEvent event = new UserEvent(DomainEventType.USER_CREATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }
}
