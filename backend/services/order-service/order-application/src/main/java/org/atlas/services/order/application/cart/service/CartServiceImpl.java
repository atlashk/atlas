package org.atlas.services.order.application.cart.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.atlas.libs.framework.cache.CacheService;
import org.atlas.libs.framework.security.AuthContext;
import org.atlas.libs.framework.util.CollectionUtil;
import org.atlas.services.order.application.cart.aggregator.CartAggregator;
import org.atlas.services.order.application.cart.constant.CartConstant;
import org.atlas.services.order.domain.entity.CartItemEntity;
import org.atlas.services.order.domain.error.DomainError;
import org.atlas.services.order.domain.exception.DomainException;
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
  public List<CartItemEntity> retrieveCart() {
    String userId = AuthContext.getUserId();
    Optional<List<CartItemEntity>> cartCache = cacheService.getList(CartConstant.CART_CACHE, userId,
        CartItemEntity.class);
    return cartCache.orElseGet(() -> loadAndCacheCartItems(userId));
  }

  @Override
  @Transactional
  public List<CartItemEntity> addCartItem(String productId, Integer quantity) {
    String userId = AuthContext.getUserId();
    cartRepository.upsertCartItem(userId, productId, quantity);
    return loadAndCacheCartItems(userId);
  }

  @Override
  @Transactional
  public List<CartItemEntity> updateQuantity(String productId, Integer quantity) {
    String userId = AuthContext.getUserId();
    if (quantity > 0) {
      cartRepository.updateQuantity(userId, productId, quantity);
    } else {
      cartRepository.removeCartItem(userId, productId);
    }
    return loadAndCacheCartItems(userId);
  }

  @Override
  @Transactional
  public List<CartItemEntity> removeCartItem(String productId) {
    String userId = AuthContext.getUserId();
    cartRepository.removeCartItem(userId, productId);
    return loadAndCacheCartItems(userId);
  }

  @Override
  @Transactional
  public List<CartItemEntity> clearCart() {
    String userId = AuthContext.getUserId();
    cartRepository.removeAllCartItems(userId);
    cacheService.evict(CartConstant.CART_CACHE, userId);
    return Collections.emptyList();
  }

  private List<CartItemEntity> loadAndCacheCartItems(String userId) {
    List<CartItemEntity> cartItems = cartRepository.findByUserId(userId);
    if (CollectionUtil.isEmpty(cartItems)) {
      return Collections.emptyList();
    }
    boolean allProductsAreValid = cartAggregator.aggregate(cartItems);
    if (!allProductsAreValid) {
      throw new DomainException(DomainError.CART_ITEM_NOT_FOUND);
    }
    cacheService.put(CartConstant.CART_CACHE, userId, cartItems);
    return cartItems;
  }
}
