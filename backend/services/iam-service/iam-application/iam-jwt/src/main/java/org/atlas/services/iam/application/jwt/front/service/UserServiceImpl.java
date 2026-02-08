package org.atlas.services.iam.application.jwt.front.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.services.iam.application.jwt.event.service.UserEventService;
import org.atlas.services.iam.application.jwt.front.mapper.UserMapper;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.front.model.ChangePasswordInput;
import org.atlas.services.iam.port.in.front.model.ProfileOutput;
import org.atlas.services.iam.port.in.front.model.RegisterInput;
import org.atlas.services.iam.port.in.front.service.UserService;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserEventService userEventService;

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

    userEventService.publishUserCreatedEvent(user);
  }

  @Override
  @Transactional
  public void changePassword(ChangePasswordInput input) {
    String userId = Contexts.getUserId();
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    if (passwordEncoder.matches(input.getOldPassword(), user.getPassword())) {
      throw new DomainException(DomainError.WRONG_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(input.getNewPassword()));
    userRepository.update(user);
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
}
