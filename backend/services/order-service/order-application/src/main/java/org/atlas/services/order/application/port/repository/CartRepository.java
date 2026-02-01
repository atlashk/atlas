package org.atlas.services.order.application.port.repository;

import java.util.Optional;
import org.atlas.services.order.domain.entity.Cart;

public interface CartRepository {

  Optional<Cart> findByUserId(Integer userId);

  void insert(Cart cart);

  void update(Cart cart);
}
