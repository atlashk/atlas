package org.atlas.services.iam.application.jwt.admin.service;

import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.paging.PagingResult;
import org.atlas.libs.framework.sequencegenerator.SequenceGenerator;
import org.atlas.libs.framework.sequencegenerator.SequenceType;
import org.atlas.libs.framework.util.MapperUtil;
import org.atlas.services.iam.application.jwt.admin.mapper.AdminUserMapper;
import org.atlas.services.iam.application.jwt.event.service.UserEventService;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminRetrieveUserListInput;
import org.atlas.services.iam.port.in.admin.model.AdminUpdateUserInput;
import org.atlas.services.iam.port.in.admin.model.AdminUserOutput;
import org.atlas.services.iam.port.in.admin.service.AdminUserService;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.atlas.services.iam.port.out.repository.UserRepository.FindUserCriteria;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

  private final UserRepository userRepository;
  private final SequenceGenerator sequenceGenerator;
  private final PasswordEncoder passwordEncoder;
  private final UserEventService userEventService;

  @Override
  @Transactional(readOnly = true)
  public PagingResult<AdminUserOutput> retrieveUserList(AdminRetrieveUserListInput input) {
    FindUserCriteria criteria = AdminUserMapper.INSTANCE.toFindUserCriteria(input);
    PagingResult<UserEntity> userPage = userRepository.findByCriteria(criteria,
        input.getPagingRequest());
    return MapperUtil.mapPage(userPage, AdminUserMapper.INSTANCE::toAdminUserOutput);
  }

  @Override
  @Transactional(readOnly = true)
  public Long retrieveUserCount() {
    return userRepository.countAll();
  }

  @Override
  @Transactional(readOnly = true)
  public AdminUserOutput retrieveUser(String id) {
    return userRepository.findById(id)
        .map(AdminUserMapper.INSTANCE::toAdminUserOutput)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public void createUser(AdminCreateUserInput input) {
    checkValidity(input);

    UserEntity user = AdminUserMapper.INSTANCE.toUser(input);
    user.setId(sequenceGenerator.generate(SequenceType.USER));
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    userRepository.insert(user);

    userEventService.publishUserCreatedEvent(user);
  }

  @Override
  @Transactional
  public void updateUser(AdminUpdateUserInput input) {
    UserEntity user = userRepository.findById(input.getId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    boolean shouldPublishEvent = !Objects.equals(input.getFirstName(), user.getFirstName()) ||
        !Objects.equals(input.getLastName(), user.getLastName());
    
    AdminUserMapper.INSTANCE.merge(input, user);
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    userRepository.update(user);

    if (shouldPublishEvent) {
      userEventService.publishUserUpdatedEvent(user);
    }
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    userRepository.deleteById(id);

    userEventService.publishUserDeletedEvent(id);
  }

  private void checkValidity(AdminCreateUserInput input) {
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
}
