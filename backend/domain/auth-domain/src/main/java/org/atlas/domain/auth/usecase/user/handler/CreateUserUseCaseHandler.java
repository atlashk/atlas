package org.atlas.domain.auth.usecase.user.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.auth.entity.User;
import org.atlas.domain.auth.repository.UserRepository;
import org.atlas.domain.auth.service.PasswordService;
import org.atlas.domain.auth.usecase.user.mapper.UserMapper;
import org.atlas.domain.auth.usecase.user.model.CreateUserInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class CreateUserUseCaseHandler {

  private final UserRepository userRepository;
  private final PasswordService passwordService;

  public void handle(CreateUserInput input) {
    User user = UserMapper.INSTANCE.toUser(input);
    user.setPassword(passwordService.encode(input.getPassword()));
    userRepository.insert(user);
  }
}
