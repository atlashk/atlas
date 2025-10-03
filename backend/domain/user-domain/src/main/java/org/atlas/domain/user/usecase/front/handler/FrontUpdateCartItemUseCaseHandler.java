package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.usecase.front.model.FrontUpdateCartItemInput;
import org.atlas.framework.cache.CachePort;
import org.atlas.framework.cache.Caches;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontUpdateCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;
  private final CachePort cachePort;

  public CartEntity handle(FrontUpdateCartItemInput input) throws Exception {
    // Find cart
    CartEntity cart = cartRepository.findByUserId(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    if (input.getQuantity() > 0) {
      cart.putCartItem(input.getProductId(), input.getQuantity());
    } else {
      cart.removeCartItem(input.getProductId());
    }
    cartRepository.update(cart);

    // Update cache
    cartAggregator.aggregate(cart);
    cachePort.put(Caches.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }
}
