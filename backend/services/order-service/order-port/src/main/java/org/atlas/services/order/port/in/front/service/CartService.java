package org.atlas.services.order.port.in.front.service;

import org.atlas.services.order.domain.entity.CartEntity;

public interface CartService {

  CartEntity retrieveCart(String userId);

  CartEntity addCartItem(String userId, String productId, Integer quantity);

  CartEntity updateQuantity(String userId, String productId, Integer quantity);

  CartEntity removeCartItem(String userId, String productId);

  CartEntity clearCart(String userId);
}
