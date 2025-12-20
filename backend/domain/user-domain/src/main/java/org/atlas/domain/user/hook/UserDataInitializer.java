package org.atlas.domain.user.hook;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.shared.Role;
import org.atlas.domain.user.usecase.front.handler.CreateUserUseCaseHandler;
import org.atlas.domain.user.usecase.front.model.CreateUserInput;
import org.atlas.framework.hook.StartupHook;

@StartupHook
@RequiredArgsConstructor
@Slf4j
public class UserDataInitializer {

  private final CreateUserUseCaseHandler createUserUseCaseHandler;

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
    createUserUseCaseHandler.handle(input);
  }
}
