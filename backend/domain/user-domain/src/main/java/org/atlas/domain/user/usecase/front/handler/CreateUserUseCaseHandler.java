package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.event.mapper.UserEventMapper;
import org.atlas.domain.user.infrastructure.messaging.UserEventMessagePublisher;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.shared.Role;
import org.atlas.domain.user.usecase.front.mapper.UserMapper;
import org.atlas.domain.user.usecase.front.model.CreateUserInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.user.UserEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.internalapi.auth.AuthApiClient;
import org.atlas.framework.internalapi.auth.model.CreateUserRequest;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class CreateUserUseCaseHandler {

  private final UserRepository userRepository;
  private final AuthApiClient authApiClient;
  private final UserEventMessagePublisher userEventMessagePublisher;

  public Void handle(CreateUserInput input) throws Exception {
    checkValidity(input);
    User user = createUser(input);
    syncUser(user, input.getPassword());
    publishEvent(user);
    return null;
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

  private User createUser(CreateUserInput input) {
    User user = UserMapper.INSTANCE.toUser(input);
    userRepository.insert(user);
    return user;
  }

  private void syncUser(User user, String password) {
    CreateUserRequest request = UserMapper.INSTANCE.toCreateUserRequest(user);
    request.setPassword(password);
    authApiClient.createUser(request);
    log.info("Created auth user: userId={}, username={}", user.getId(), user.getUsername());
  }

  private void publishEvent(User user) {
    UserEvent event = new UserEvent(DomainEventType.USER_CREATED);
    UserEventMapper.INSTANCE.merge(user, event);
    userEventMessagePublisher.publish(event);
  }
}
