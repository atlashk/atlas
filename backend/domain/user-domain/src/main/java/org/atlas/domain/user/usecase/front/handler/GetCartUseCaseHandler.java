package org.atlas.domain.user.usecase.front.handler;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.aggregator.CartAggregator;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.GetCartInput;
import org.atlas.framework.cache.Cache;
import org.atlas.framework.domain.usecase.UseCaseHandler;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class GetCartUseCaseHandler {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;

  @Cache(cacheName = "cart", key = "#input.userId")
  public CartEntity handle(GetCartInput input) throws Exception {
    // Get or create cart for user
    Optional<CartEntity> cartOpt = cartRepository.findByUserId(input.getUserId());
    if (cartOpt.isEmpty()) {
      return new CartEntity(input.getUserId());
    }
    CartEntity cart = cartOpt.get();

    // Fetch products
    if (cart.hasItems()) {
      boolean allProductsAreValid = cartAggregator.aggregate(cart);

      // Update cart if necessary
      if (!allProductsAreValid) {
        cartRepository.update(cart);
      }
    }

    return cart;
  }
}
