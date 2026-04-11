package org.atlas.services.order.port.out.repository;

import java.util.List;
import org.atlas.services.order.domain.entity.CartItem;

public interface CartRepository {

  List<CartItem> findByUserId(String userId);

  void upsertCartItem(String userId, String productId, Integer quantity);

  void updateQuantity(String userId, String productId, Integer quantity);

  void removeCartItem(String userId, String productId);

  void removeAllCartItems(String userId);
}
