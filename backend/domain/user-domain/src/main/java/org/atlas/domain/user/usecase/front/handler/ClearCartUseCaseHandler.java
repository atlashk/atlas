package org.atlas.domain.user.usecase.front.handler;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.ClearCartInput;
import org.atlas.framework.cache.CacheService;
import org.atlas.framework.cache.ApplicationCache;
import org.atlas.framework.domain.error.DomainError;
import org.atlas.framework.domain.exception.DomainException;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
public class ClearCartUseCaseHandler {

  private final CartRepository cartRepository;
  private final CacheService cacheService;

  public Cart handle(ClearCartInput input) throws Exception {
    // Find cart
    Cart cart = cartRepository.findByUserId(input.getUserId())
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.clear();
    cartRepository.update(cart);

    // Update cache
    cacheService.put(ApplicationCache.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }
}
