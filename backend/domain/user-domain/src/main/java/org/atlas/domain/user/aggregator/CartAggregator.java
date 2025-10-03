package org.atlas.domain.user.aggregator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.entity.CartItemEntity;
import org.atlas.domain.user.entity.ProductEntity;
import org.atlas.framework.internalapi.product.ProductApiPort;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.CollectionUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartAggregator {

  private final ProductApiPort productApiPort;

  public boolean aggregate(CartEntity cart) {
    if (cart == null) {
      throw new IllegalArgumentException("Cart must be provided");
    }
    return loadProducts(cart);
  }

  /**
   * @return false if at least one product is no longer available
   */
  private boolean loadProducts(CartEntity cart) {
    List<Integer> productIds = cart.getProductIds();
    ListProductRequest request = new ListProductRequest(productIds);
    List<ProductResponse> productResponses = productApiPort.call(request);

    if (CollectionUtil.isEmpty(productResponses)) {
      log.error("All products are no longer available, clearing cart {}", cart.getId());
      cart.clearItems();
      return false;
    }

    // Update cart item's product
    Map<Integer, ProductResponse> productResponseMap = productResponses.stream()
        .collect(Collectors.toMap(ProductResponse::getId, Function.identity()));
    boolean allProductsAreValid = true;
    for (CartItemEntity cartItem : cart.getCartItems()) {
      Integer productId = cartItem.getProduct().getId();
      ProductResponse productResponse = productResponseMap.get(productId);
      if (productResponse != null) {
        ProductEntity product = ObjectMapperUtil.getInstance()
            .map(productResponse, ProductEntity.class);
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
