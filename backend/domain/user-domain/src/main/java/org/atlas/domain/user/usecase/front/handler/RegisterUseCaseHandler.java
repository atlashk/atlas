package org.atlas.domain.user.usecase.front.handler;

import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.event.mapper.UserEventMapper;
import org.atlas.domain.user.infrastructure.messaging.UserEventMessagePublisher;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.shared.Role;
import org.atlas.domain.user.usecase.front.mapper.UserMapper;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.auth.client.AuthClient;
import org.atlas.framework.auth.client.model.CreateAuthUserRequest;
import org.atlas.framework.cryptography.PasswordUtil;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.event.DomainEventType;
import org.atlas.framework.domain.event.contract.user.UserEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class RegisterUseCaseHandler {

  private final UserRepository userRepository;
  private final @Nullable AuthClient authClient;
  private final UserEventMessagePublisher userEventMessagePublisher;

  public Void handle(RegisterInput input) throws Exception {
    checkValidity(input);
    User user = createUser(input);
    syncUser(user);
    publishEvent(user);
    return null;
  }

  private void checkValidity(RegisterInput input) {
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

  private User createUser(RegisterInput input) {
    User user = UserMapper.INSTANCE.toUser(input);
    user.setPassword(PasswordUtil.hashPassword(input.getPassword()));
    user.setRole(Role.USER);
    userRepository.insert(user);
    return user;
  }

  private void syncUser(User user) {
    if (authClient != null) {
      CreateAuthUserRequest request = UserMapper.INSTANCE.toCreateAuthUserRequest(user);
      authClient.createAuthUser(request);
      log.info("Created auth user: userId={}, username={}",
          user.getId(), user.getUsername());
    }
  }

  private void publishEvent(User user) {
    UserEvent event = new UserEvent(DomainEventType.USER_REGISTERED);
    UserEventMapper.INSTANCE.merge(user, event);
    userEventMessagePublisher.publish(event);
  }
}
