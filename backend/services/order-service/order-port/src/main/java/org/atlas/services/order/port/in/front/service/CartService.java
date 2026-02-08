package org.atlas.services.order.port.in.front.service;

import org.atlas.services.order.domain.entity.CartEntity;

public interface CartService {

  CartEntity retrieveCart();

  CartEntity addCartItem(String productId, Integer quantity);

  CartEntity updateQuantity(String productId, Integer quantity);

  CartEntity removeCartItem(String productId);

  CartEntity clearCart();
}
