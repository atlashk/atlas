package org.atlas.services.identity.application.jwt.user.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.event.DomainEventType;
import org.atlas.libs.framework.domain.common.event.contract.identity.UserCreatedEvent;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.security.authorization.RequiredAdmin;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.identity.application.jwt.user.mapper.UserAdminMapper;
import org.atlas.services.identity.application.jwt.user.mapper.UserEventMapper;
import org.atlas.services.identity.domain.entity.UserEntity;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.RetrieveUserListInput;
import org.atlas.services.identity.port.in.user.model.admin.UpdateUserInput;
import org.atlas.services.identity.port.in.user.model.admin.UserOutput;
import org.atlas.services.identity.port.in.user.service.UserAdminService;
import org.atlas.services.identity.port.out.messaging.UserEventMessagePublisher;
import org.atlas.services.identity.port.out.repository.UserRepository;
import org.atlas.services.identity.port.out.repository.UserRepository.FindUserCriteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredAdmin
@RequiredArgsConstructor
public class UserAdminServiceImpl implements UserAdminService {

  private final UserRepository userRepository;
  private final SequenceGenerator sequenceGenerator;
  private final PasswordEncoder passwordEncoder;
  private final UserEventMessagePublisher messagePublisher;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<UserOutput> retrieveUserList(RetrieveUserListInput input) {
    FindUserCriteria criteria = UserAdminMapper.INSTANCE.toFindUserCriteria(input);
    PagingResult<UserEntity> userPage = userRepository.findByCriteria(criteria,
        input.getPagingRequest());
    return MapperUtil.mapPage(userPage, UserAdminMapper.INSTANCE::toUser);
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveUserCount() {
    return userRepository.countAll();
  }

  @Override
  @Transactional(readOnly = true)
  public UserOutput retrieveUser(String id) {
    return userRepository.findById(id)
        .map(UserAdminMapper.INSTANCE::toUser)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public void createUser(CreateUserInput input) {
    checkValidity(input);

    UserEntity user = UserAdminMapper.INSTANCE.toUser(input);
    user.setId(sequenceGenerator.generate(SequenceType.USER));
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    userRepository.insert(user);

    publishUserCreatedEvent(user);
  }

  @Override
  @Transactional
  public void updateUser(UpdateUserInput input) {
    UserEntity user = userRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    boolean shouldPublishEvent = !Objects.equals(input.getFirstName(), user.getFirstName()) ||
        !Objects.equals(input.getLastName(), user.getLastName());
    
    UserAdminMapper.INSTANCE.merge(input, user);
    userRepository.update(user);

    if (shouldPublishEvent) {
      publishUserUpdatedEvent(user);
    }
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    userRepository.deleteById(id);

    publishUserDeletedEvent(id);
  }

  @Override
  public boolean existsUser(String username) {
    return userRepository.existsByUsername(username);
  }

  private void checkValidity(CreateUserInput input) {
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
    UserCreatedEvent event = new UserCreatedEvent(DomainEventType.USER_CREATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }

  private void publishUserUpdatedEvent(UserEntity user) {
    UserCreatedEvent event = new UserCreatedEvent(DomainEventType.USER_UPDATED);
    UserEventMapper.INSTANCE.merge(user, event);
    messagePublisher.publish(event);
  }

  private void publishUserDeletedEvent(String userId) {
    UserCreatedEvent event = new UserCreatedEvent(DomainEventType.USER_DELETED);
    event.setUserId(userId);
    messagePublisher.publish(event);
  }
}
