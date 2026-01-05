package org.atlas.user.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.user.application.event.mapper.UserEventMapper;
import org.atlas.user.application.event.mapper.UserMapper;
import org.atlas.user.application.model.CreateUserInput;
import org.atlas.user.application.port.messaging.UserEventMessagePublisher;
import org.atlas.user.application.port.repository.UserRepository;
import org.atlas.user.domain.entity.User;
import org.atlas.common.framework.domain.common.error.DomainError;
import org.atlas.common.framework.domain.common.event.DomainEventType;
import org.atlas.common.framework.domain.common.event.contract.user.UserEvent;
import org.atlas.common.framework.domain.common.exception.DomainException;
import org.atlas.common.framework.internalapi.auth.AuthApiClient;
import org.atlas.common.framework.internalapi.auth.model.CreateUserRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final AuthApiClient authApiClient;
  private final UserEventMessagePublisher userEventMessagePublisher;

  @Override
  @Transactional(readOnly = true)
  public User retrieveUser(Integer userId) {
    return userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public void createUser(CreateUserInput input) {
    checkValidity(input);

    User user = UserMapper.INSTANCE.toUser(input);
    userRepository.insert(user);

    syncUser(user, input.getPassword());

    publishUserCreatedEvent(user);
  }

  private void checkValidity(CreateUserInput input) {
    if (userRepository.findByUsername(input.getUsername()).isPresent()) {
      throw new DomainException(DomainError.USERNAME_ALREADY_EXISTS);
    }
    if (userRepository.findByEmail(input.getEmail()).isPresent()) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.findByPhoneNumber(input.getPhoneNumber()).isPresent()) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }

  private void syncUser(User user, String password) {
    CreateUserRequest request = UserMapper.INSTANCE.toCreateUserRequest(user);
    request.setPassword(password);
    authApiClient.createUser(request);
    log.info("Created auth user: userId={}, username={}", user.getId(), user.getUsername());
  }

  private void publishUserCreatedEvent(User user) {
    UserEvent event = new UserEvent(DomainEventType.USER_CREATED);
    UserEventMapper.INSTANCE.merge(user, event);
    userEventMessagePublisher.publish(event);
  }
}
