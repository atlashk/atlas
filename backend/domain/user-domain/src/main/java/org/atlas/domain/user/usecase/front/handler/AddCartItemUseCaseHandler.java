package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.AddCartItemInput;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.cache.Caches;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class AddCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;
  private final CacheService cacheService;

  public CartEntity handle(AddCartItemInput input) throws Exception {
    // Get or create cart for user
    CartEntity cart = cartRepository.findByUserId(input.getUserId())
        .orElseGet(() -> {
          // Create new cart
          CartEntity newCart = new CartEntity(input.getUserId());
          cartRepository.insert(newCart);
          return newCart;
        });

    // Update DB
    cart.putCartItem(input.getProductId(), input.getQuantity());
    cartRepository.update(cart);

    // Update cache
    cartAggregator.aggregate(cart);
    cacheService.put(Caches.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }
}
