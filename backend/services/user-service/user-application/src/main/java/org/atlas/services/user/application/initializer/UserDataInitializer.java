package org.atlas.services.user.application.initializer;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.shared.user.UserRole;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.services.user.port.in.model.admin.CreateUserInput;
import org.atlas.services.user.port.in.service.UserAdminService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

  private final UserAdminService userAdminService;
  private final ApplicationConfigService applicationConfigService;

  @EventListener(ApplicationReadyEvent.class)
  @Transactional
  public void initialize(ApplicationReadyEvent event) {
    // Set context first
    SecurityContextUtil.setContextForSystemAdmin();

    try {
      if (!userAdminService.existsUser("admin@atlas.org")) {
        createAdminUser();
      }

      if (!userAdminService.existsUser("demo@atlas.org")) {
        createDemoUser();
      }
    } catch (Exception e) {
      // Fail-fast
      log.error("Failed to initialize user data", e);
      SpringApplication.exit(event.getApplicationContext());
    }
  }

  private void createAdminUser() throws Exception {
    CreateUserInput input = CreateUserInput.builder()
        .password(getDefaultPassword())
        .firstName("John")
        .lastName("Doe")
        .email("admin@atlas.org")
        .phone("0987654321")
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
        .phone("0123456789")
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
