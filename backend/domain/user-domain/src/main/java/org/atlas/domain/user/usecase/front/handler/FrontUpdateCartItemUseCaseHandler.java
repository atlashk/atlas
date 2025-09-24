package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.entity.CartItemEntity;
import org.atlas.domain.user.repository.CartItemRepository;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.FrontUpdateCartItemInput;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontUpdateCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  public CartEntity handle(FrontUpdateCartItemInput input) throws Exception {
    Integer userId = Contexts.getUserId();

    // Find cart
    CartEntity cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Find cart item
    CartItemEntity item = cartItemRepository.findByCartIdAndProductId(cart.getId(),
            input.getProductId())
        .orElseThrow(() -> new DomainException(DomainError.CART_ITEM_NOT_FOUND));

    if (input.getQuantity() <= 0) {
      // Remove item if quantity is 0 or negative
      cartItemRepository.delete(item.getId());
    } else {
      // Update quantity
      item.setQuantity(input.getQuantity());
      cartItemRepository.update(item);
    }

    // Update cart
    cart.setItems(cartItemRepository.findByCartId(cart.getId()));
    cart.updateComputedFields();
    cartRepository.update(cart);
    return cart;
  }
}