package org.atlas.services.iam.application.jwt.hook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.user.UserRole;
import org.atlas.libs.framework.hook.StartupHook;
import org.atlas.services.iam.port.in.admin.model.AdminCreateUserInput;
import org.atlas.services.iam.port.in.admin.service.AdminUserService;

@StartupHook
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

  private final AdminUserService adminUserService;

  public void handle() throws Exception {
    if (!adminUserService.existsUser("admin")) {
      createAdminUser();
    }

    if (!adminUserService.existsUser("demo")) {
      createDemoUser();
    }
  }

  private void createAdminUser() throws Exception {
    AdminCreateUserInput input = AdminCreateUserInput.builder()
        .username("admin")
        .password("Aa@123456")
        .firstName("John")
        .lastName("Doe")
        .email("admin@atlas.org")
        .phoneNumber("0987654321")
        .role(UserRole.ADMIN)
        .build();
    adminUserService.createUser(input);
    log.info("Created admin user");
  }

  private void createDemoUser() throws Exception {
    AdminCreateUserInput input = AdminCreateUserInput.builder()
        .username("demo")
        .password("Aa@123456")
        .firstName("Demo")
        .lastName("User")
        .email("demo@atlas.org")
        .phoneNumber("0123456789")
        .role(UserRole.USER)
        .build();
    adminUserService.createUser(input);
    log.info("Created demo user");
  }
}
