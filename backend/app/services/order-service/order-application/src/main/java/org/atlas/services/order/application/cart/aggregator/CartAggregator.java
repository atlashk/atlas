package org.atlas.services.order.application.cart.aggregator;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.internal.catalog.client.ProductApiClient;
import org.atlas.libs.framework.internal.catalog.model.ProductOutput;
import org.atlas.libs.framework.internal.catalog.model.RetrieveProductListInput;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.application.cart.mapper.CartMapper;
import org.atlas.services.order.domain.entity.CartItem;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartAggregator {

  private final ProductApiClient productApiClient;

  public boolean aggregate(List<CartItem> cartItems) {
    if (cartItems == null) {
      throw new IllegalArgumentException("Cart items must be provided");
    }
    return loadProducts(cartItems);
  }

  /**
   * @return false if at least one product is no longer available
   */
  private boolean loadProducts(List<CartItem> cartItems) {
    List<String> productIds = cartItems.stream()
        .map(CartItem::getProduct)
        .flatMap(product -> product == null ? Stream.empty() : Stream.of(product))
        .map(CartItem.Product::getId)
        .toList();
    RetrieveProductListInput request = new RetrieveProductListInput(productIds);
    List<ProductOutput> products = productApiClient.call(request);

    if (CollectionUtil.isEmpty(products)) {
      log.error("All products are no longer available, clearing cart");
      cartItems.clear();
      return false;
    }

    // Update cart item's product
    Map<String, ProductOutput> productMap = products.stream()
        .collect(Collectors.toMap(ProductOutput::getId, Function.identity()));
    for (CartItem cartItem : cartItems) {
      String productId = cartItem.getProduct().getId();
      ProductOutput product = productMap.get(productId);
      if (product != null) {
        cartItem.setProduct(CartMapper.INSTANCE.toProduct(product));
      } else {
        log.error("Product {} no longer exists, removing from cart item {}",
            productId, cartItem.getId());
        return false;
      }
    }
    return true;
  }
}
