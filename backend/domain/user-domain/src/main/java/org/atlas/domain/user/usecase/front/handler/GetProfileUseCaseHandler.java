package org.atlas.domain.user.usecase.front.handler;

import javax.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.User;
import org.atlas.domain.user.repository.UserRepository;
import org.atlas.domain.user.usecase.front.model.GetProfileInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class GetProfileUseCaseHandler {

  private final UserRepository userRepository;

  public User handle(@Nonnull GetProfileInput input) throws Exception {
    return userRepository.findById(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.USER_NOT_FOUND));
  }
}
