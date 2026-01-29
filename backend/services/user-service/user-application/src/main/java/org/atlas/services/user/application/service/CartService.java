package org.atlas.services.user.application.service;

import org.atlas.services.user.domain.entity.Cart;

public interface CartService {

  Cart retrieveCart(Integer userId);

  Cart addCartItem(Integer userId, Integer productId, Integer quantity);

  Cart updateQuantity(Integer userId, Integer productId, Integer quantity);

  Cart removeCartItem(Integer userId, Integer productId);

  Cart clearCart(Integer userId);
}
