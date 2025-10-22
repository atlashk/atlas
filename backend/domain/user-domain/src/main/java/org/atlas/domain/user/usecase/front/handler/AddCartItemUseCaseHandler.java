package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.AddCartItemInput;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.cache.Caches;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.springframework.dao.DataIntegrityViolationException;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class AddCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;
  private final CacheService cacheService;

  public CartEntity handle(AddCartItemInput input) throws Exception {
    // Get or create cart for user
    CartEntity cart = cartRepository.findByUserId(input.getUserId())
        .orElseGet(() -> {
          // Create new cart
          try {
            CartEntity newCart = new CartEntity(input.getUserId());
            cartRepository.insert(newCart);
            return newCart;
          } catch (DataIntegrityViolationException e) {
            // If a duplicate occurs, try finding it again
            log.error("Duplicate cart record for user {}", input.getUserId(), e);
            return cartRepository.findByUserId(input.getUserId()).get();
          }
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
