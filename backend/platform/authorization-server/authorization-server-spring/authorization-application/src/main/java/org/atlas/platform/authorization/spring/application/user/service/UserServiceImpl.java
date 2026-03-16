package org.atlas.platform.authorization.spring.application.user.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.platform.authorization.domain.entity.UserEntity;
import org.atlas.platform.authorization.domain.error.DomainError;
import org.atlas.platform.authorization.domain.exception.DomainException;
import org.atlas.platform.authorization.port.in.user.model.ProfileOutput;
import org.atlas.platform.authorization.port.in.user.model.RegisterInput;
import org.atlas.platform.authorization.port.in.user.service.UserService;
import org.atlas.platform.authorization.port.out.repository.UserRepository;
import org.atlas.platform.authorization.spring.application.user.mapper.UserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public ProfileOutput retrieveProfile() {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
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
  }

  private void checkValidity(RegisterInput input) {
    if (userRepository.existsByEmail(input.getEmail())) {
      throw new DomainException(DomainError.EMAIL_ALREADY_EXISTS);
    }

    if (StringUtil.isNotBlank(input.getPhone()) &&
        userRepository.existsByPhone(input.getPhone())) {
      throw new DomainException(DomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
