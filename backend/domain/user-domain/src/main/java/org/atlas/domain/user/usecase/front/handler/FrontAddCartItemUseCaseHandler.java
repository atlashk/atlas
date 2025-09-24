package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.entity.CartItemEntity;
import org.atlas.domain.user.repository.CartItemRepository;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.FrontAddCartItemInput;
import org.atlas.framework.context.Contexts;
import org.atlas.framework.domain.usecase.UseCaseHandler;

import java.util.Date;
import java.util.Optional;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontAddCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  public CartEntity handle(FrontAddCartItemInput input) throws Exception {
    Integer userId = Contexts.getUserId();

    // Get or create cart for user
    CartEntity cart = getOrCreateCartForUser(userId);

    // Check if item already exists in cart
    Optional<CartItemEntity> existingItem = cartItemRepository
        .findByCartIdAndProductId(cart.getId(), input.getProductId());

    if (existingItem.isPresent()) {
      // Update quantity
      CartItemEntity item = existingItem.get();
      item.setQuantity(item.getQuantity() + input.getQuantity());
      cartItemRepository.update(item);
    } else {
      // Add new item
      CartItemEntity newItem = CartItemEntity.builder()
          .cartId(cart.getId())
          .productId(input.getProductId())
          .quantity(input.getQuantity())
          .build();
      cartItemRepository.insert(newItem);
    }

    // Update cart
    cart.setItems(cartItemRepository.findByCartId(cart.getId()));
    cart.updateComputedFields();
    cartRepository.update(cart);
    return cart;
  }

  private CartEntity getOrCreateCartForUser(Integer userId) {
    Optional<CartEntity> existingCart = cartRepository.findByUserId(userId);
    if (existingCart.isPresent()) {
      CartEntity cart = existingCart.get();
      // Load cart items
      cart.setItems(cartItemRepository.findByCartId(cart.getId()));
      return cart;
    }

    // Create new cart
    CartEntity newCart = CartEntity.builder()
        .userId(userId)
        .lastUpdated(new Date())
        .totalItems(0)
        .build();
    cartRepository.insert(newCart);
    return newCart;
  }
}