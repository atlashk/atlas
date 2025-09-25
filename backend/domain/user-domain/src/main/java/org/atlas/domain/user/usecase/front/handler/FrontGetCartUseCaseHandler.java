package org.atlas.domain.user.usecase.front.handler;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.entity.CartItemEntity;
import org.atlas.domain.user.entity.ProductEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.domain.user.usecase.front.model.FrontGetCartInput;
import org.atlas.framework.cache.Cache;
import org.atlas.framework.cache.Caches;
import org.atlas.framework.domain.usecase.UseCaseHandler;
import org.atlas.framework.internalapi.product.ProductApiPort;
import org.atlas.framework.internalapi.product.model.ListProductRequest;
import org.atlas.framework.internalapi.product.model.ProductResponse;
import org.atlas.framework.objectmapper.ObjectMapperUtil;
import org.atlas.framework.util.CollectionUtil;

@UseCaseHandler
@RequiredArgsConstructor
@Slf4j
public class FrontGetCartUseCaseHandler {

  private final CartRepository cartRepository;
  private final ProductApiPort productApiPort;

  @Cache(cacheName = Caches.CART, key = "#input.userId")
  public CartEntity handle(FrontGetCartInput input) throws Exception {
    // Get or create cart for user
    Optional<CartEntity> cartOpt = cartRepository.findByUserId(input.getUserId());
    if (cartOpt.isEmpty()) {
      return new CartEntity(input.getUserId());
    }
    CartEntity cart = cartOpt.get();

    // Fetch products
    List<Integer> productIds = cart.getProductIds();
    ListProductRequest request = new ListProductRequest(productIds);
    List<ProductResponse> productResponses = productApiPort.call(request);
    boolean hasCartUpdates = false;
    if (CollectionUtil.isNotEmpty(productResponses)) {
      // Update cart item's product
      Map<Integer, ProductResponse> productResponseMap = productResponses.stream()
          .collect(Collectors.toMap(ProductResponse::getId, Function.identity()));
      for (CartItemEntity cartItem : cart.getCartItems()) {
        Integer productId = cartItem.getProduct().getId();
        ProductResponse productResponse = productResponseMap.get(productId);
        if (productResponse != null) {
          ProductEntity product = ObjectMapperUtil.getInstance()
              .map(productResponse, ProductEntity.class);
          cartItem.setProduct(product);
        } else {
          log.error("Product {} no longer exists, removing from cart", productId);
          cart.removeCartItem(productId);
          hasCartUpdates = true;
        }
      }
    }

    // Update cart if necessary
    if (hasCartUpdates) {
      cartRepository.update(cart);
    }

    return cart;
  }
}