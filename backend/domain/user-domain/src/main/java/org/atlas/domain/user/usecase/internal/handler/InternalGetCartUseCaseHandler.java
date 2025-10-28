package org.atlas.domain.user.usecase.internal.handler;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.entity.Cart;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.internal.model.InternalGetCartInput;
import org.atlas.framework.cache.Cache;
import org.atlas.framework.domain.usecase.ReadOnlyUseCaseHandler;

@ReadOnlyUseCaseHandler
@RequiredArgsConstructor
public class InternalGetCartUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;

  @Cache(cacheName = "cart", key = "#input.userId")
  public Cart handle(InternalGetCartInput input) throws Exception {
    // Get or create cart for user
    Optional<Cart> cartOpt = cartRepository.findByUserId(input.getUserId());
    if (cartOpt.isEmpty()) {
      return new Cart(input.getUserId());
    }
    Cart cart = cartOpt.get();

    // Fetch products
    boolean allProductsAreValid = cartAggregator.aggregate(cart);

    // Update cart if necessary
    if (!allProductsAreValid) {
      cartRepository.update(cart);
    }

    return cart;
  }
}
