package org.atlas.domain.user.usecase.internal.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.internal.model.InternalGetCartInput;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class InternalGetCartUseCaseHandler {

  private final CartRepository cartRepository;

  public CartEntity handle(InternalGetCartInput input) throws Exception {
    return cartRepository.findByUserId(input.getUserId())
        .orElseGet(() -> new CartEntity(input.getUserId()));
  }
}
