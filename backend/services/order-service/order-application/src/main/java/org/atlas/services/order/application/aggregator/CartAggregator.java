package org.atlas.services.order.application.aggregator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.internalapi.catalog.client.ProductApiClient;
import org.atlas.libs.framework.internalapi.catalog.model.ListProductRequest;
import org.atlas.libs.framework.internalapi.catalog.model.ProductResponse;
import org.atlas.services.order.application.event.mapper.CartMapper;
import org.atlas.services.order.domain.entity.Cart;
import org.atlas.services.order.domain.entity.CartItem;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartAggregator {

  private final ProductApiClient productApiClient;

  public boolean aggregate(Cart cart) {
    if (cart == null) {
      throw new IllegalArgumentException("Cart must be provided");
    }
    return loadProducts(cart);
  }

  /**
   * @return false if at least one product is no longer available
   */
  private boolean loadProducts(Cart cart) {
    List<Integer> productIds = cart.collectProductIds();
    ListProductRequest request = new ListProductRequest(productIds);
    List<ProductResponse> productResponses = productApiClient.call(request);

    if (CollectionUtil.isEmpty(productResponses)) {
      log.error("All products are no longer available, clearing cart {}", cart.getId());
      cart.clear();
      return false;
    }

    // Update cart item's product
    Map<Integer, ProductResponse> productResponseMap = productResponses.stream()
        .collect(Collectors.toMap(ProductResponse::getId, Function.identity()));
    boolean allProductsAreValid = true;
    for (CartItem cartItem : cart.getCartItems()) {
      Integer productId = cartItem.getProduct().getId();
      ProductResponse productResponse = productResponseMap.get(productId);
      if (productResponse != null) {
        CartItem.Product product = CartMapper.INSTANCE.toProduct(productResponse);
        cartItem.setProduct(product);
      } else {
        log.error("Product {} no longer exists, removing from cart {}", productId, cart.getId());
        cart.removeCartItem(productId);
        allProductsAreValid = false;
      }
    }
    return allProductsAreValid;
  }
}
