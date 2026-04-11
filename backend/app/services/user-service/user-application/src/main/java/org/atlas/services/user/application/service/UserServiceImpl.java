package org.atlas.services.user.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.StringUtil;
import org.atlas.services.user.port.in.model.ProfileOutput;
import org.atlas.services.user.port.in.model.RegisterInput;
import org.atlas.services.user.port.in.service.UserService;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.application.mapper.UserMapper;
import org.atlas.services.user.domain.entity.User;
import org.atlas.services.user.domain.error.UserDomainError;
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
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));
  }

  @Override
  @Transactional
  public void register(RegisterInput input) {
    checkValidity(input);

    User user = UserMapper.INSTANCE.toUser(input);
    user.setPassword(passwordEncoder.encode(input.getPassword()));
    user.setRole(UserRole.USER);
    userRepository.insert(user);
  }

  private void checkValidity(RegisterInput input) {
    if (userRepository.existsByEmail(input.getEmail())) {
      throw new DomainException(UserDomainError.EMAIL_ALREADY_EXISTS);
    }

    if (StringUtil.isNotBlank(input.getPhoneNumber()) &&
        userRepository.existsByPhoneNumber(input.getPhoneNumber())) {
      throw new DomainException(UserDomainError.PHONE_NUMBER_ALREADY_EXISTS);
    }
  }
}
