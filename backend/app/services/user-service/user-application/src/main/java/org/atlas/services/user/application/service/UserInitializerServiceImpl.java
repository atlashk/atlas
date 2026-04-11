package org.atlas.services.user.application.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.services.user.port.in.model.admin.CreateUserInput;
import org.atlas.services.user.port.in.service.UserAdminService;
import org.atlas.services.user.port.in.service.UserInitializerService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserInitializerServiceImpl implements UserInitializerService {

  private final UserAdminService userAdminService;
  private final ApplicationConfigService applicationConfigService;

  @Async
  @Override
  @Transactional
  public void initializeUserData() throws Exception {
    SecurityContextUtil.setContextForSystemAdmin();

    if (!userAdminService.existsUser("admin@atlas.org")) {
      createAdminUser();
    }

    if (!userAdminService.existsUser("demo@atlas.org")) {
      createDemoUser();
    }

    log.info("User data initialization completed.");
  }

  private void createAdminUser() throws Exception {
    CreateUserInput input = CreateUserInput.builder()
        .password(getDefaultPassword())
        .firstName("John")
        .lastName("Doe")
        .email("admin@atlas.org")
        .phoneNumber("0987654321")
        .role(UserRole.ADMIN)
        .build();
    userAdminService.createUser(input);
    log.info("Created admin user");
  }

  private void createDemoUser() throws Exception {
    CreateUserInput input = CreateUserInput.builder()
        .password(getDefaultPassword())
        .firstName("Demo")
        .lastName("User")
        .email("demo@atlas.org")
        .phoneNumber("0123456789")
        .role(UserRole.USER)
        .build();
    userAdminService.createUser(input);
    log.info("Created demo user");
  }

  private String getDefaultPassword() {
    return Optional.ofNullable(applicationConfigService.getConfig("security.default-password"))
        .orElseThrow(() -> new RuntimeException("Default password not set"));
  }
}
