package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartItemRepository;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.FrontRemoveCartItemInput;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontRemoveCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  public CartEntity handle(FrontRemoveCartItemInput input) throws Exception {
    Integer userId = Contexts.getUserId();

    // Find cart
    CartEntity cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Remove item from cart
    cartItemRepository.deleteByCartIdAndProductId(cart.getId(), input.getProductId());

    // Update cart
    cart.setItems(cartItemRepository.findByCartId(cart.getId()));
    cart.updateComputedFields();
    cartRepository.update(cart);
    return cart;
  }
}
