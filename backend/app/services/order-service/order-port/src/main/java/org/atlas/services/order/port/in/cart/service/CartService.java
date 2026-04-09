package org.atlas.services.order.port.in.cart.service;

import java.util.List;
import org.atlas.services.order.domain.entity.CartItemEntity;

public interface CartService {

  List<CartItemEntity> retrieveCart();

  List<CartItemEntity> addCartItem(String productId, Integer quantity);

  List<CartItemEntity> updateQuantity(String productId, Integer quantity);

  List<CartItemEntity> removeCartItem(String productId);

  List<CartItemEntity> clearCart();
}
