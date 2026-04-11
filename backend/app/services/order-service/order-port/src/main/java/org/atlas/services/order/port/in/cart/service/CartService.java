package org.atlas.services.order.port.in.cart.service;

import java.util.List;
import org.atlas.services.order.domain.entity.CartItem;

public interface CartService {

  List<CartItem> retrieveCart();

  List<CartItem> addCartItem(String productId, Integer quantity);

  List<CartItem> updateQuantity(String productId, Integer quantity);

  List<CartItem> removeCartItem(String productId);

  List<CartItem> clearCart();
}
