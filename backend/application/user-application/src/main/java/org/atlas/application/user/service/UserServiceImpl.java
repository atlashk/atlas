package org.atlas.application.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.application.user.event.mapper.UserEventMapper;
import org.atlas.application.user.mapper.UserMapper;
import org.atlas.application.user.model.CreateUserInput;
import org.atlas.application.user.port.messaging.UserEventMessagePublisher;
import org.atlas.application.user.port.repository.UserRepository;
import org.atlas.domain.user.entity.User;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.user.UserEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;
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
