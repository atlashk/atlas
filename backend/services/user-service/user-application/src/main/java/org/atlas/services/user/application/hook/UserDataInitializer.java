package org.atlas.services.user.application.hook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.domain.user.Role;
import org.atlas.libs.framework.hook.StartupHook;
import org.atlas.services.user.application.model.CreateUserInput;
import org.atlas.services.user.application.service.UserService;

@StartupHook
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

  private final UserService userService;

  public void handle() throws Exception {
    log.info("Creating admin user...");
    createAdminUser();
    log.info("Created admin user successfully");
  }

  private void createAdminUser() throws Exception {
    CreateUserInput input = CreateUserInput.builder()
        .username("admin")
        .password("Aa@123456")
        .firstName("John")
        .lastName("Doe")
        .email("admin@atlas.org")
        .phoneNumber("0987654321")
        .role(Role.ADMIN)
        .build();
    userService.createUser(input);
  }
}
