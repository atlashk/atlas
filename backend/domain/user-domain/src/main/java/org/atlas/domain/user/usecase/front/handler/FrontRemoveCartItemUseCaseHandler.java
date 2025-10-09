package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.usecase.front.model.FrontRemoveCartItemInput;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.cache.Caches;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class FrontRemoveCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;
  private final CacheService cacheService;

  public CartEntity handle(FrontRemoveCartItemInput input) throws Exception {
    // Find cart
    CartEntity cart = cartRepository.findByUserId(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.removeCartItem(input.getProductId());
    cartRepository.update(cart);

    // Update cache
    cartAggregator.aggregate(cart);
    cacheService.put(Caches.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }
}
