package org.atlas.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.atlas.domain.user.entity.CartEntity;
import org.atlas.domain.user.repository.CartRepository;
import org.atlas.framework.domain.service.DomainService;

@DomainService
@RequiredArgsConstructor
public class CartService {

  private final CartRepository cartRepository;

  public CartEntity getOrCreateCart(Integer userId) {
    return cartRepository.findByUserId(userId)
        .orElseGet(() -> {
          // Create new cart
          CartEntity newCart = new CartEntity(userId);
          cartRepository.insert(newCart);
          return newCart;
        });
  }
}
