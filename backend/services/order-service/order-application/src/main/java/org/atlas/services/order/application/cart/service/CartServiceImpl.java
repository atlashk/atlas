package org.atlas.services.order.application.cart.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.ApplicationCache;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.context.Contexts;
import org.atlas.libs.framework.domain.error.DomainError;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.application.cart.aggregator.CartAggregator;
import org.atlas.services.order.domain.entity.CartEntity;
import org.atlas.services.order.port.in.cart.service.CartService;
import org.atlas.services.order.port.out.repository.CartRepository;
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
  @Transactional(readOnly = true)
  public CartEntity retrieveCart() {
    String userId = Contexts.getUserId();
    
    // Apply cache-aside pattern
    Optional<CartEntity> cartCache = cacheService.get(ApplicationCache.CART, userId, CartEntity.class);
    if (cartCache.isPresent()) {
      return cartCache.get();
    }

    // Get or create cart for user
    Optional<CartEntity> cartOpt = cartRepository.findByUserId(userId);
    if (cartOpt.isEmpty()) {
      return CartEntity.builder()
          .userId(userId)
          .build();
    }
    CartEntity cart = cartOpt.get();

    // Fetch products
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      boolean allProductsAreValid = cartAggregator.aggregate(cart);

      // Update cart if necessary
      if (!allProductsAreValid) {
        cartRepository.update(cart);
      }
    }

    // Update cache
    cacheService.put(ApplicationCache.CART, userId, cart);

    return cart;
  }

  @Override
  @Transactional
  public CartEntity addCartItem(String productId, Integer quantity) {
    String userId = Contexts.getUserId();

    // Get or create cart for user
    CartEntity cart = cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          // Create new cart
          CartEntity newCart = CartEntity.builder()
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
    cacheService.put(ApplicationCache.CART, cart.getUserId(), cart);

    return cart;
  }

  @Override
  @Transactional
  public CartEntity updateQuantity(String productId, Integer quantity) {
    String userId = Contexts.getUserId();

    // Find cart
    CartEntity cart = cartRepository.findByUserId(userId)
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
    cacheService.put(ApplicationCache.CART, cart.getUserId(), cart);

    return cart;
  }

  @Override
  @Transactional
  public CartEntity removeCartItem(String productId) {
    String userId = Contexts.getUserId();

    // Find cart
    CartEntity cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.removeCartItem(productId);
    cartRepository.update(cart);

    // Update cache
    if (CollectionUtil.isNotEmpty(cart.getCartItems())) {
      cartAggregator.aggregate(cart);
      cacheService.put(ApplicationCache.CART, cart.getUserId(), cart);
    }

    return cart;
  }

  @Override
  @Transactional
  public CartEntity clearCart() {
    String userId = Contexts.getUserId();

    // Find cart
    CartEntity cart = cartRepository.findByUserId(userId)
        .orElseThrow(() -> new DomainException(DomainError.CART_NOT_FOUND));

    // Update DB
    cart.clear();
    cartRepository.update(cart);

    // Update cache
    cacheService.put(ApplicationCache.CART, cart.getUserId(), cart);

    return cart;
  }
}
