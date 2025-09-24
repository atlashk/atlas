package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartItemRepository;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontClearCartUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  public CartEntity handle(Void input) throws Exception {
    Integer userId = Contexts.getUserId();

    // Find cart
    CartEntity cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Clear all items from cart
    cartItemRepository.deleteByCartId(cart.getId());

    // Update cart
    cart.clearItems();
    cart.updateComputedFields();
    cartRepository.update(cart);
    return cart;
  }
}