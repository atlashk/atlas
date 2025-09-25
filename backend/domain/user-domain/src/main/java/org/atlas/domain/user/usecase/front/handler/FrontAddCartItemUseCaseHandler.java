package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.FrontAddCartItemInput;
import org.atlas.framework.cache.CachePort;
import org.atlas.framework.cache.Caches;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontAddCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CachePort cachePort;

  public CartEntity handle(FrontAddCartItemInput input) throws Exception {
    // Get or create cart for user
    CartEntity cart = cartRepository.findByUserId(input.getUserId())
        .orElseGet(() -> {
          // Create new cart
          CartEntity newCart = new CartEntity(input.getUserId());
          cartRepository.insert(newCart);
          return newCart;
        });

    // Update cart
    cart.putCartItem(input.getProductId(), input.getQuantity());
    cartRepository.update(cart);

    // Invalidate cache
    cachePort.invalidate(Caches.CART, String.valueOf(input.getUserId()));

    return cart;
  }
}
