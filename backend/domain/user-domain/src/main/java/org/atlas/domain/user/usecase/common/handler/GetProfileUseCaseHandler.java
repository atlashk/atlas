package org.atlas.domain.user.usecase.common.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.handler.UseCaseHandler;
import org.atlas.framework.domain.error.DomainError;

@UseCaseHandler
@RequiredArgsConstructor
public class GetProfileUseCaseHandler {

  private final UserRepository userRepository;

  public UserEntity handle(Void input) throws Exception {
    Integer userId = Contexts.getUserId();
    return userRepository.findById(userId)
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }
}
