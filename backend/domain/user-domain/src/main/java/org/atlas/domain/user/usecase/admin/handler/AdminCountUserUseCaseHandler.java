package org.atlas.domain.user.usecase.admin.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class AdminCountUserUseCaseHandler {

  private final UserRepository userRepository;

  public Long handle() throws Exception {
    return userRepository.countAll();
  }
}
