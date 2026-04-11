package org.atlas.services.order.application.cart.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.domain.exception.DomainException;
import org.atlas.libs.framework.security.SecurityContextUtil;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.application.cart.aggregator.CartAggregator;
import org.atlas.services.order.application.cart.constant.CartConstant;
import org.atlas.services.order.domain.entity.CartItem;
import org.atlas.services.order.domain.error.OrderDomainError;
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
  public List<CartItem> retrieveCart() {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    Optional<List<CartItem>> cartCache = cacheService.getList(CartConstant.CART_CACHE, userId,
        CartItem.class);
    return cartCache.orElseGet(() -> loadAndCacheCartItems(userId));
  }

  @Override
  @Transactional
  public List<CartItem> addCartItem(String productId, Integer quantity) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    cartRepository.upsertCartItem(userId, productId, quantity);
    return loadAndCacheCartItems(userId);
  }

  @Override
  @Transactional
  public List<CartItem> updateQuantity(String productId, Integer quantity) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    if (quantity > 0) {
      cartRepository.updateQuantity(userId, productId, quantity);
    } else {
      cartRepository.removeCartItem(userId, productId);
    }
    return loadAndCacheCartItems(userId);
  }

  @Override
  @Transactional
  public List<CartItem> removeCartItem(String productId) {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    cartRepository.removeCartItem(userId, productId);
    return loadAndCacheCartItems(userId);
  }

  @Override
  @Transactional
  public List<CartItem> clearCart() {
    String userId = SecurityContextUtil.requirePrincipal().getUserId();
    cartRepository.removeAllCartItems(userId);
    cacheService.evict(CartConstant.CART_CACHE, userId);
    return Collections.emptyList();
  }

  private List<CartItem> loadAndCacheCartItems(String userId) {
    List<CartItem> cartItems = cartRepository.findByUserId(userId);
    if (CollectionUtil.isEmpty(cartItems)) {
      return Collections.emptyList();
    }
    boolean allProductsAreValid = cartAggregator.aggregate(cartItems);
    if (!allProductsAreValid) {
      throw new DomainException(OrderDomainError.CART_ITEM_NOT_FOUND);
    }
    cacheService.put(CartConstant.CART_CACHE, userId, cartItems);
    return cartItems;
  }
}
