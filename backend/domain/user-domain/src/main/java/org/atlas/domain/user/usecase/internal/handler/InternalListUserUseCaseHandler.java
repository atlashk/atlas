package org.atlas.domain.user.usecase.internal.handler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.usecase.internal.model.InternalListUserInput;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class InternalListUserUseCaseHandler {

  private final UserRepository userRepository;

  public List<UserEntity> handle(InternalListUserInput input) throws Exception {
    return userRepository.findByIdIn(input.getIds());
  }
}
