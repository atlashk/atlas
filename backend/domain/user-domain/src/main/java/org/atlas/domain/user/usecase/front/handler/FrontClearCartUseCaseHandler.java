package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.FrontClearCartInput;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontClearCartUseCaseHandler {

  private final CartRepository cartRepository;

  public CartEntity handle(FrontClearCartInput input) throws Exception {
    // Find cart
    CartEntity cart = cartRepository.findByUserId(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update cart
    cart.clearItems();
    cartRepository.update(cart);
    return cart;
  }
}
