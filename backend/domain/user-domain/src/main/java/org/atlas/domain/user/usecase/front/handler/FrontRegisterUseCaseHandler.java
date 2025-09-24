package org.atlas.domain.user.usecase.front.handler;

import javax.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.shared.Role;
import org.atlas.domain.user.usecase.front.model.RegisterInput;
import org.atlas.framework.auth.client.AuthClientPort;
import org.atlas.framework.auth.client.model.CreateAuthUserRequest;
import org.atlas.framework.config.ApplicationConfigPort;
import org.atlas.framework.domain.event.contract.user.UserRegisteredEvent;
import org.atlas.framework.domain.event.contract.user.model.User;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.messaging.ExternalMessagePublisherPort;
import org.atlas.framework.objectmapper.ObjectMapperUtil;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class FrontRegisterUseCaseHandler {

  private final UserRepository userRepository;
  private final @Nullable AuthClientPort authClientPort;
  private final ApplicationConfigPort applicationConfigPort;
  private final ExternalMessagePublisherPort externalMessagePublisherPort;

  public Void handle(RegisterInput input) throws Exception {
    checkValidity(input);
    UserEntity userEntity = createUser(input);
    syncUser(userEntity);
    publishEvent(userEntity);
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

  private UserEntity createUser(RegisterInput input) {
    UserEntity userEntity = ObjectMapperUtil.getInstance().map(input, UserEntity.class);
    userEntity.setRole(Role.USER);
    userRepository.insert(userEntity);
    return userEntity;
  }

  private void syncUser(UserEntity userEntity) {
    if (authClientPort != null) {
      CreateAuthUserRequest request = ObjectMapperUtil.getInstance()
          .map(userEntity, CreateAuthUserRequest.class);
      authClientPort.createAuthUser(request);
      log.info("Created auth user: userId={}, username={}",
          userEntity.getId(), userEntity.getUsername());
    }
  }

  private void publishEvent(UserEntity userEntity) {
    User user = ObjectMapperUtil.getInstance().map(userEntity, User.class);
    UserRegisteredEvent event = new UserRegisteredEvent(applicationConfigPort.getApplicationName(),
        user);
    externalMessagePublisherPort.publish(event);
  }
}
