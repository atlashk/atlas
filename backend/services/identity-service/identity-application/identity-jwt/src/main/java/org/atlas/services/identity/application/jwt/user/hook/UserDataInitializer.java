package org.atlas.services.identity.application.jwt.user.hook;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.config.ApplicationConfigService;
import org.atlas.libs.framework.domain.shared.identity.UserRole;
import org.atlas.libs.framework.hook.StartupHook;
import org.atlas.services.identity.port.in.user.model.admin.CreateUserInput;
import org.atlas.services.identity.port.in.user.service.UserAdminService;

@StartupHook
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

  private final UserAdminService userAdminService;
  private final ApplicationConfigService applicationConfigService;

  public void handle() throws Exception {
    if (!userAdminService.existsUser("admin")) {
      createAdminUser();
    }

    if (!userAdminService.existsUser("demo")) {
      createDemoUser();
    }
  }

  private void createAdminUser() throws Exception {
    CreateUserInput input = CreateUserInput.builder()
        .username("admin")
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
        .username("demo")
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
