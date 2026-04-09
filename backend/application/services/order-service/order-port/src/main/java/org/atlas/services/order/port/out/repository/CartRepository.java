package org.atlas.services.order.port.out.repository;

import java.util.List;
import org.atlas.services.order.domain.entity.CartItemEntity;

public interface CartRepository {

  List<CartItemEntity> findByUserId(String userId);

  void upsertCartItem(String userId, String productId, Integer quantity);

  void updateQuantity(String userId, String productId, Integer quantity);

  void removeCartItem(String userId, String productId);

  void removeAllCartItems(String userId);
}
