package org.atlas.domain.user.usecase.common.handler;

import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.UserEntity;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.usecase.common.model.GetProfileInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.springframework.transaction.annotation.Transactional;

@UseCaseHandler
@RequiredArgsConstructor
public class GetProfileUseCaseHandler {

  private final UserRepository userRepository;

  @Transactional(readOnly = true)
  public UserEntity handle(@Nonnull GetProfileInput input) throws Exception {
    return userRepository.findById(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }
}
