package org.atlas.services.order.application.front.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.ApplicationCache;
import org.atlas.libs.framework.cache.Cache;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.collection.CollectionUtil;
import org.atlas.libs.framework.domain.common.error.DomainError;
import org.atlas.libs.framework.domain.common.exception.DomainException;
import org.atlas.services.order.application.front.aggregator.CartAggregator;
import org.atlas.services.order.port.out.repository.CartRepository;
import org.atlas.services.order.domain.entity.Cart;
import org.atlas.services.order.port.in.front.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

  private final CartRepository cartRepository;
  private final CartAggregator cartAggregator;
  private final CacheService cacheService;

  @Override
  @Cache(cacheName = "cart", key = "#userId")
  @Transactional(readOnly = true)
  public Cart retrieveCart(Integer userId) {
    // Get or create cart for user
    Optional<Cart> cartOpt = cartRepository.findByUserId(userId);
    if (cartOpt.isEmpty()) {
      return Cart.builder()
          .userId(userId)
          .build();
    }
    Cart cart = cartOpt.get();

    // Fetch products
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      boolean allProductsAreValid = cartAggregator.aggregate(cart);

      // Update cart if necessary
      if (!allProductsAreValid) {
        cartRepository.update(cart);
      }
    }

    return cart;
  }

  @Override
  @Transactional
  public Cart addCartItem(Integer userId, Integer productId, Integer quantity) {
    // Get or create cart for user
    Cart cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          // Create new cart
          Cart newCart = Cart.builder()
              .userId(userId)
              .build();
          cartRepository.insert(newCart);
          return newCart;
        });

    // Add cart item and update DB
    cart.addCartItem(productId, quantity);
    cartRepository.update(cart);

    // Update cache
    cartAggregator.aggregate(cart);
    cacheService.put(ApplicationCache.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }

  @Override
  @Transactional
  public Cart updateQuantity(Integer userId, Integer productId, Integer quantity) {
    // Find cart
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    if (quantity > 0) {
      cart.setCartItemQuantity(productId, quantity);
    } else {
      cart.removeCartItem(productId);
    }
    cartRepository.update(cart);

    // Update cache
    cartAggregator.aggregate(cart);
    cacheService.put(ApplicationCache.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }

  @Override
  @Transactional
  public Cart removeCartItem(Integer userId, Integer productId) {
    // Find cart
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.removeCartItem(productId);
    cartRepository.update(cart);

    // Update cache
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cartAggregator.aggregate(cart);
      cacheService.put(ApplicationCache.CART, String.valueOf(cart.getUserId()), cart);
    }

    return cart;
  }

  @Override
  @Transactional
  public Cart clearCart(Integer userId) {
    // Find cart
    Cart cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.clear();
    cartRepository.update(cart);

    // Update cache
    cacheService.put(ApplicationCache.CART, String.valueOf(cart.getUserId()), cart);

    return cart;
  }
}
