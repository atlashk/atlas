package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.RemoveCartItemInput;
import org.atlas.framework.cache.ApplicationCache;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
public class RemoveCartItemUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;
  private final CacheService cacheService;

  public Cart handle(RemoveCartItemInput input) throws Exception {
    // Find cart
    Cart cart = cartRepository.findByUserId(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.removeCartItem(input.getProductId());
    cartRepository.update(cart);

    // Update cache
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cartAggregator.aggregate(cart);
      cacheService.put(ApplicationCache.CART, String.valueOf(cart.getUserId()), cart);
    }

    return cart;
  }
}
