package org.atlas.services.iam.application.jwt.authentication;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.libs.framework.security.authorization.RequiredAdmin;
import org.atlas.services.iam.domain.entity.UserEntity;
import org.atlas.services.iam.port.in.authentication.service.AuthenticationAdminService;
import org.atlas.services.iam.port.out.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredAdmin
@RequiredArgsConstructor
public class AuthenticationAdminServiceImpl implements AuthenticationAdminService {

  private final UserRepository userRepository;
  private final ApplicationConfigService applicationConfigService;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void resetPassword(String userId) throws Exception {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));

    String defaultPassword = Optional.ofNullable(
            applicationConfigService.getConfig("security.default-password"))
        .orElseThrow(() -> new RuntimeException("Default password not set"));
    String encodedPassword = passwordEncoder.encode(defaultPassword);
    user.setPassword(encodedPassword);
    userRepository.update(user);
  }
}
