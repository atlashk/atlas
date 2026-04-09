package org.atlas.edge.authorization.api.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.services.user.port.out.repository.UserRepository;
import org.atlas.services.user.domain.entity.UserEntity;
import org.atlas.services.user.domain.error.UserDomainError;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuthenticationAdminService {

  private final UserRepository userRepository;
  private final ApplicationConfigService applicationConfigService;
  private final PasswordEncoder passwordEncoder;

  @Transactional
  public void resetPassword(String userId) throws Exception {
    UserEntity user = userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(UserDomainError.USER_NOT_FOUND));

    String defaultPassword = Optional.ofNullable(
            applicationConfigService.getConfig("security.default-password"))
        .orElseThrow(() -> new RuntimeException("Default password not set"));
    String encodedPassword = passwordEncoder.encode(defaultPassword);
    user.setPassword(encodedPassword);
    userRepository.update(user);
  }
}
