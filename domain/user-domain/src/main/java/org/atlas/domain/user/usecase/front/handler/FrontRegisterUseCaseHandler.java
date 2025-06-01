package org.atlas.domain.user.usecase.front.handler;

import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.port.messaging.UserMessagePublisherPort;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.shared.enums.Role;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.auth.client.AuthClientPort;
import org.atlas.framework.auth.client.model.CreateUserRequest;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.user.UserRegisteredEvent;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.error.AppError;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class FrontRegisterUseCaseHandler {

  private final UserRepository userRepository;
  private final AuthClientPort authClientPort;
  private final ApplicationConfigPort applicationConfigPort;
  private final UserMessagePublisherPort userMessagePublisherPort;

  public Void handle(RegisterInput input) throws Exception {
    checkValidity(input);
    UserEntity userEntity = createUser(input);
    createAuthUser(userEntity);
    publishEvent(userEntity);
    return null;
  }

  private void checkValidity(RegisterInput input) {
    if (userRepository.findByUsername(input.getUsername()).isPresent()) {
      throw new DomainException(AppError.USERNAME_ALREADY_EXISTS);
    }
    if (userRepository.findByEmail(input.getEmail()).isPresent()) {
      throw new DomainException(AppError.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.findByPhoneNumber(input.getPhoneNumber()).isPresent()) {
      throw new DomainException(AppError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }

  private UserEntity createUser(RegisterInput input) {
    UserEntity userEntity = ObjectMapperUtil.getInstance()
        .map(input, UserEntity.class);
    userEntity.setRole(Role.USER);
    userRepository.insert(userEntity);
    return userEntity;
  }

  private void createAuthUser(UserEntity userEntity) {
    CreateUserRequest request = ObjectMapperUtil.getInstance()
        .map(userEntity, CreateUserRequest.class);
    authClientPort.createUser(request);
    log.info("Created auth user: userId={}, username={}",
        userEntity.getId(), userEntity.getUsername());
  }

  private void publishEvent(UserEntity userEntity) {
    UserRegisteredEvent event = new UserRegisteredEvent(applicationConfigPort.getApplicationName());
    ObjectMapperUtil.getInstance()
        .merge(userEntity, event);
    event.setUserId(userEntity.getId());
    userMessagePublisherPort.publish(event);
  }
}
